package com.lisory.backend.carrinho.services;

import com.lisory.backend.auth.entity.AuthEntity;
import com.lisory.backend.carrinho.dto.CartItemResponse;
import com.lisory.backend.carrinho.dto.CartRequest;
import com.lisory.backend.carrinho.dto.CartResponse;
import com.lisory.backend.carrinho.dto.CartUpdateRequest;
import com.lisory.backend.carrinho.entity.Cart;
import com.lisory.backend.carrinho.entity.CartItem;
import com.lisory.backend.carrinho.repository.CartItemRepository;
import com.lisory.backend.carrinho.repository.CartRepository;
import com.lisory.backend.exception.BusinessException;
import com.lisory.backend.exception.ResourceNotFoundException;
import com.lisory.backend.produtos.entity.Product;
import com.lisory.backend.produtos.entity.ProductImage;
import com.lisory.backend.produtos.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId, UUID guestCartId) {
        Optional<Cart> cartOpt = Optional.empty();

        if (userId != null) {
            cartOpt = cartRepository.findByUserId(userId);
        } else if (guestCartId != null) {
            cartOpt = cartRepository.findByGuestCartId(guestCartId);
        }

        return cartOpt.map(this::toResponse)
                .orElse(new CartResponse(null, List.of(), BigDecimal.ZERO));
    }

    @Transactional
    public CartResponse addItem(UUID userId, UUID guestCartId, CartRequest request) {
        Cart cart = findOrCreateCart(userId, guestCartId);

        Product product = productRepository.findBySlug(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", request.productId()));

        if (product.getStockQuantity() <= 0) {
            throw new BusinessException("Product is out of stock");
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getSlug().equals(request.productId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int addQty = request.quantity() != null ? request.quantity() : 1;
            int newQty = item.getQuantity() + addQty;
            if (newQty > product.getStockQuantity()) {
                throw new BusinessException("Requested quantity exceeds available stock (" + product.getStockQuantity() + ")");
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.quantity() != null ? request.quantity() : 1);
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }

        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(UUID userId, UUID guestCartId, UUID itemId, CartUpdateRequest request) {
        Cart cart = findOrCreateCart(userId, guestCartId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("CartItem", "id", itemId);
        }

        if (request.quantity() <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(request.quantity());
            cartItemRepository.save(item);
        }

        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(UUID userId, UUID guestCartId, UUID itemId) {
        Cart cart = findOrCreateCart(userId, guestCartId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("CartItem", "id", itemId);
        }

        cartItemRepository.delete(item);

        return toResponse(cart);
    }

    @Transactional
    public void clearCart(UUID userId, UUID guestCartId) {
        Cart cart = findOrCreateCart(userId, guestCartId);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    private Cart findOrCreateCart(UUID userId, UUID guestCartId) {
        if (userId != null) {
            return cartRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        Cart cart = new Cart();
                        cart.setUser(entityManager.getReference(AuthEntity.class, userId));
                        return cartRepository.save(cart);
                    });
        }

        if (guestCartId != null) {
            return cartRepository.findByGuestCartId(guestCartId)
                    .orElseGet(() -> {
                        Cart cart = new Cart();
                        cart.setGuestCartId(guestCartId);
                        return cartRepository.save(cart);
                    });
        }

        Cart cart = new Cart();
        cart.setGuestCartId(UUID.randomUUID());
        return cartRepository.save(cart);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> {
                    Product product = item.getProduct();
                    String primaryImage = product.getImages().stream()
                            .filter(ProductImage::getPrimary)
                            .findFirst()
                            .map(ProductImage::getImageUrl)
                            .orElse(null);

                    BigDecimal unitPrice = product.getPromotionalPrice() != null
                            ? product.getPromotionalPrice()
                            : product.getPrice();
                    BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

                    return new CartItemResponse(
                            item.getId(),
                            product.getId(),
                            product.getName(),
                            primaryImage,
                            unitPrice,
                            item.getQuantity(),
                            total
                    );
                })
                .toList();

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), items, subtotal);
    }
}
