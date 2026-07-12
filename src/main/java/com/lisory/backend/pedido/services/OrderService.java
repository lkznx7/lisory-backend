package com.lisory.backend.pedido.services;

import com.lisory.backend.auth.entity.AuthEntity;
import com.lisory.backend.auth.entity.ROLES;
import com.lisory.backend.auth.repository.AuthRepository;
import com.lisory.backend.carrinho.entity.Cart;
import com.lisory.backend.carrinho.entity.CartItem;
import com.lisory.backend.carrinho.repository.CartItemRepository;
import com.lisory.backend.carrinho.repository.CartRepository;
import com.lisory.backend.cupons.entity.Coupon;
import com.lisory.backend.cupons.repository.CouponRepository;
import com.lisory.backend.cupons.services.CouponService;
import com.lisory.backend.envios.repository.ShipmentRepository;
import com.lisory.backend.envios.services.ShipmentService;
import com.lisory.backend.envios.services.ShippingQuote;
import com.lisory.backend.exception.BusinessException;
import com.lisory.backend.exception.InvalidOperationException;
import com.lisory.backend.exception.ResourceNotFoundException;
import com.lisory.backend.pagamentos.repository.PaymentRepository;
import com.lisory.backend.pagamentos.services.PaymentService;
import com.lisory.backend.pedido.dto.OrderRequest;
import com.lisory.backend.pedido.dto.OrderResponse;
import com.lisory.backend.pedido.entity.Order;
import com.lisory.backend.pedido.entity.OrderItem;
import com.lisory.backend.pedido.entity.OrderStatus;
import com.lisory.backend.pedido.repository.OrderRepository;
import com.lisory.backend.produtos.entity.Product;
import com.lisory.backend.produtos.repository.ProductRepository;
import com.lisory.backend.user.entity.Address;
import com.lisory.backend.user.repository.AddressRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final CouponRepository couponRepository;
    private final CouponService couponService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final ShipmentService shipmentService;
    private final ShipmentRepository shipmentRepository;
    private final OrderResponseMapper responseMapper;
    private final AuthRepository authRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        AddressRepository addressRepository,
                        CouponRepository couponRepository,
                        CouponService couponService,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        PaymentService paymentService,
                        PaymentRepository paymentRepository,
                        ShipmentService shipmentService,
                        ShipmentRepository shipmentRepository,
                        OrderResponseMapper responseMapper,
                        AuthRepository authRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.couponRepository = couponRepository;
        this.couponService = couponService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
        this.shipmentService = shipmentService;
        this.shipmentRepository = shipmentRepository;
        this.responseMapper = responseMapper;
        this.authRepository = authRepository;
    }

    public OrderResponse createFromCart(UUID userId, UUID guestCartId, OrderRequest request) {
        Cart cart;
        if (userId != null) {
            cart = cartRepository.findByUserId(userId)
                    .orElse(null);
        } else if (guestCartId != null) {
            cart = cartRepository.findByGuestCartId(guestCartId)
                    .orElse(null);
        } else {
            throw new BusinessException("Cart identifier is required: provide userId or guestCartId");
        }
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "id", userId != null ? userId : guestCartId);
        }
        cart.getItems().size();

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        Address address = null;
        if (request.addressId() != null) {
            address = addressRepository.findById(request.addressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.addressId()));
        }

        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> {
                    Product product = item.getProduct();
                    BigDecimal price = product.getPromotionalPrice() != null
                            ? product.getPromotionalPrice() : product.getPrice();
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        Coupon coupon = null;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            coupon = couponService.validateAndApply(request.couponCode(), subtotal, request.guestEmail());
            discount = calculateDiscount(coupon, subtotal);
        }

        Order order = new Order();
        if (userId != null) {
            order.setUser(entityManager.getReference(AuthEntity.class, userId));
        }
        order.setAddress(address);
        order.setCoupon(coupon);
        order.setStatus(OrderStatus.AGUARDANDO_PAGAMENTO.name());
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setShippingCost(BigDecimal.ZERO);
        order.setTotal(subtotal.subtract(discount));
        order.setGuestName(request.guestName());
        order.setGuestEmail(request.guestEmail());
        order.setGuestPhone(request.guestPhone());
        order.setGuestCpf(request.guestCpf());

        Set<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    Product p = cartItem.getProduct();
                    BigDecimal price = p.getPromotionalPrice() != null
                            ? p.getPromotionalPrice() : p.getPrice();
                    orderItem.setUnitPrice(price);
                    return orderItem;
                })
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        paymentService.processOrderPayment(savedOrder.getId(), request.paymentMethod(), savedOrder.getTotal());

        ShippingQuote defaultQuote = new ShippingQuote("PAC", "PAC", BigDecimal.ZERO, 0);
        shipmentService.createShipment(savedOrder.getId(), defaultQuote);

        cartItemRepository.deleteByCartId(cart.getId());

        return responseMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return responseMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findByUser(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(responseMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            return orderRepository.findByStatus(status, pageable).map(responseMapper::toResponse);
        }
        return orderRepository.findAll(pageable).map(responseMapper::toResponse);
    }

    public OrderResponse updateStatus(UUID id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid order status: " + status);
        }

        OrderStatus currentStatus = OrderStatus.valueOf(order.getStatus());

        if (!isValidTransition(currentStatus, newStatus)) {
            throw new InvalidOperationException(
                    "Cannot transition from " + currentStatus + " to " + newStatus);
        }

        order.setStatus(newStatus.name());
        Order saved = orderRepository.save(order);
        return responseMapper.toResponse(saved);
    }

    public OrderResponse cancelOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        OrderStatus currentStatus = OrderStatus.valueOf(order.getStatus());

        if (currentStatus == OrderStatus.CANCELADO) {
            throw new InvalidOperationException("Order is already cancelled");
        }

        if (currentStatus == OrderStatus.ENTREGUE) {
            throw new InvalidOperationException("Cannot cancel an order that has already been delivered");
        }

        order.setStatus(OrderStatus.CANCELADO.name());
        Order saved = orderRepository.save(order);
        return responseMapper.toResponse(saved);
    }

    private boolean isValidTransition(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case AGUARDANDO_PAGAMENTO -> next == OrderStatus.PAGO || next == OrderStatus.CANCELADO;
            case PAGO -> next == OrderStatus.PROCESSANDO || next == OrderStatus.CANCELADO;
            case PROCESSANDO -> next == OrderStatus.ENVIADO || next == OrderStatus.CANCELADO;
            case ENVIADO -> next == OrderStatus.ENTREGUE || next == OrderStatus.CANCELADO;
            case ENTREGUE -> false;
            case CANCELADO -> false;
        };
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        long totalOrders = orderRepository.count();
        long paidOrders = orderRepository.countByStatus("PAGO");
        long pendingOrders = orderRepository.countByStatus("AGUARDANDO_PAGAMENTO");
        BigDecimal totalRevenue = orderRepository.sumTotalByStatus("PAGO");

        return Map.of(
                "totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                "totalOrders", totalOrders,
                "paidOrders", paidOrders,
                "pendingOrders", pendingOrders,
                "totalProducts", productRepository.count(),
                "totalCustomers", authRepository.countByRole(ROLES.USER)
        );
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon == null) return BigDecimal.ZERO;
        if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
            return subtotal.multiply(coupon.getDiscountValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else if ("FIXED".equalsIgnoreCase(coupon.getDiscountType())) {
            BigDecimal discount = coupon.getDiscountValue();
            return discount.compareTo(subtotal) > 0 ? subtotal : discount;
        }
        return BigDecimal.ZERO;
    }
}
