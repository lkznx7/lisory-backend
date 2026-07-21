package com.lisory.backend.pedido.services;

import com.lisory.backend.auth.entity.AuthEntity;
import com.lisory.backend.carrinho.entity.Cart;
import com.lisory.backend.carrinho.entity.CartItem;
import com.lisory.backend.carrinho.repository.CartItemRepository;
import com.lisory.backend.carrinho.repository.CartRepository;
import com.lisory.backend.cupons.services.CouponService;
import com.lisory.backend.envios.services.ShipmentService;
import com.lisory.backend.envios.services.ShippingQuote;
import com.lisory.backend.exception.BusinessException;
import com.lisory.backend.exception.ResourceNotFoundException;
import com.lisory.backend.cupons.entity.Coupon;
import com.lisory.backend.pagamentos.services.PaymentService;
import com.lisory.backend.pedido.dto.OrderRequest;
import com.lisory.backend.pedido.dto.OrderResponse;
import com.lisory.backend.pedido.entity.Order;
import com.lisory.backend.pedido.entity.OrderItem;
import com.lisory.backend.pedido.entity.OrderStatus;
import com.lisory.backend.pedido.repository.OrderRepository;
import com.lisory.backend.produtos.entity.Product;
import com.lisory.backend.user.entity.Address;
import com.lisory.backend.user.repository.AddressRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderFacade {

    private static final Logger log = LoggerFactory.getLogger(OrderFacade.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final CouponService couponService;
    private final PaymentService paymentService;
    private final ShipmentService shipmentService;
    private final OrderResponseMapper responseMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public OrderFacade(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            AddressRepository addressRepository,
            CouponService couponService,
            PaymentService paymentService,
            ShipmentService shipmentService,
            OrderResponseMapper responseMapper) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.addressRepository = addressRepository;
        this.couponService = couponService;
        this.paymentService = paymentService;
        this.shipmentService = shipmentService;
        this.responseMapper = responseMapper;
    }

    @Transactional
    public OrderResponse checkout(UUID userId, UUID guestCartId, OrderRequest request) {
        Cart cart = findCart(userId, guestCartId);

        log.info("checkout_cart_found userId={} guestCartId={} cartId={} itemCount={}",
                userId, guestCartId, cart.getId(), cart.getItems().size());

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        Address address = resolveAddress(request.addressId(), request);
        BigDecimal subtotal = calculateSubtotal(cart);

        BigDecimal discount = BigDecimal.ZERO;
        Coupon coupon = null;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            coupon = couponService.validateAndApply(request.couponCode(), subtotal, request.guestEmail());
            discount = calculateDiscount(coupon, subtotal);
        }

        BigDecimal shippingCost = request.shippingCost() != null ? request.shippingCost() : BigDecimal.ZERO;

        Order order = createOrder(userId, address, coupon, subtotal, discount, request);
        order.setStatus(OrderStatus.PENDING_PAYMENT.name());
        order.setShippingCost(shippingCost);
        order.setShippingCarrier(request.shippingCarrier());
        order.setShippingService(request.shippingService());
        order.setTotal(subtotal.subtract(discount).add(shippingCost));
        Order savedOrder = orderRepository.save(order);

        Set<OrderItem> items = createOrderItems(savedOrder, cart);
        savedOrder.setItems(items);
        orderRepository.save(savedOrder);

        decrementStock(cart);

        cartItemRepository.deleteByCartId(cart.getId());

        String carrier = request.shippingCarrier() != null ? request.shippingCarrier() : "PAC";
        String service = request.shippingService() != null ? request.shippingService() : "PAC";
        ShippingQuote shippingQuote = new ShippingQuote(carrier, service, shippingCost, 0);
        shipmentService.createShipment(savedOrder.getId(), shippingQuote);

        paymentService.initiatePayment(savedOrder.getId(), request.paymentMethod(), savedOrder.getTotal());

        return responseMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse confirmPayment(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus currentStatus = OrderStatus.valueOf(order.getStatus());
        if (currentStatus != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException("Order is not awaiting payment");
        }

        order.setStatus(OrderStatus.PAGO.name());
        Order savedOrder = orderRepository.save(order);

        String carrier = order.getShippingCarrier() != null ? order.getShippingCarrier() : "PAC";
        String service = order.getShippingService() != null ? order.getShippingService() : "PAC";
        ShippingQuote defaultQuote = new ShippingQuote(carrier, service, BigDecimal.ZERO, 0);
        shipmentService.createShipment(orderId, defaultQuote);

        return responseMapper.toResponse(savedOrder);
    }

    public Cart findCart(UUID userId, UUID guestCartId) {
        Cart cart;
        if (userId != null) {
            cart = cartRepository.findByUserId(userId)
                    .orElse(null);
            if (cart != null) return cart;
        }
        if (guestCartId != null) {
            cart = cartRepository.findByGuestCartId(guestCartId)
                    .orElse(null);
            if (cart != null) {
                cart.getItems().size();
                return cart;
            }
        }
        throw new BusinessException("Cart not found");
    }

    private Address resolveAddress(UUID addressId, OrderRequest request) {
        if (addressId != null) {
            return addressRepository.findById(addressId)
                    .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        }
        if (request.street() != null && request.zipCode() != null) {
            Address addr = new Address();
            addr.setStreet(request.street());
            addr.setNumber(request.number());
            addr.setComplement(request.complement());
            addr.setNeighborhood(request.neighborhood());
            addr.setCity(request.city());
            addr.setState(request.state());
            addr.setZipCode(request.zipCode());
            addr.setCountry("BR");
            return addressRepository.save(addr);
        }
        return null;
    }

    private BigDecimal calculateSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> {
                    Product product = item.getProduct();
                    BigDecimal price = product.getPromotionalPrice() != null
                            ? product.getPromotionalPrice() : product.getPrice();
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order createOrder(UUID userId, Address address, Coupon coupon,
                              BigDecimal subtotal, BigDecimal discount, OrderRequest request) {
        Order order = new Order();
        if (userId != null) {
            order.setUser(entityManager.getReference(AuthEntity.class, userId));
        }
        order.setAddress(address);
        order.setCoupon(coupon);
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setGuestName(request.guestName());
        order.setGuestEmail(request.guestEmail());
        order.setGuestPhone(request.guestPhone());
        order.setGuestCpf(request.guestCpf());
        return order;
    }

    private void decrementStock(Cart cart) {
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            int newStock = product.getStockQuantity() - cartItem.getQuantity();
            product.setStockQuantity(Math.max(0, newStock));
        }
    }

    private Set<OrderItem> createOrderItems(Order order, Cart cart) {
        return cart.getItems().stream()
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
