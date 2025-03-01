package com.packshop.api.modules.shopping.services;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packshop.api.common.exceptions.ResourceNotFoundException;
import com.packshop.api.modules.catalog.repositories.ProductRepository;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.dto.CartDTO;
import com.packshop.api.modules.shopping.dto.CartItemDTO;
import com.packshop.api.modules.shopping.entities.cart.Cart;
import com.packshop.api.modules.shopping.entities.cart.CartItem;
import com.packshop.api.modules.shopping.repositories.CartItemRepository;
import com.packshop.api.modules.shopping.repositories.CartRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public CartDTO getCartByUser(User user) {
        Cart cart = user.getCart();
        if (cart == null) {
            cart = createCart(user);
        }
        return convertToCartDTO(cart);
    }

    @Transactional
    public Cart createCart(User user) {
        Cart cart = user.getCart();
        if (cart != null)
            return cart;

        cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);
        return cartRepository.save(cart);
    }

    @Transactional
    public CartItemDTO addItemToCart(User user, Long productId, int quantity) {
        log.info("Adding product {} with quantity {} to user {}'s cart", productId, quantity, user.getUsername());

        // Validate product existence
        if (!productRepository.existsById(productId)) {
            log.error("Product not found with id: {}", productId);
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        // Validate quantity
        if (quantity <= 0) {
            log.error("Invalid quantity: {}", quantity);
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // Get or create cart
        Cart cart = user.getCart() != null ? user.getCart() : createCart(user);

        // Check for existing item using repository instead of stream
        CartItem item = cartItemRepository.findByCartAndProduct(cart, productId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(productId);
                    newItem.setQuantity(0); // Will be updated below
                    cart.addCartItem(newItem);
                    return newItem;
                });

        // Update quantity and save
        item.setQuantity(item.getQuantity() + quantity);
        CartItem savedItem = cartItemRepository.save(item);
        log.info("Cart item updated/created with id: {}, quantity: {}", savedItem.getId(), savedItem.getQuantity());

        return convertToCartItemDTO(savedItem);
    }

    @Transactional
    public CartItemDTO updateCartItem(User user, Long itemId, int quantity) {
        log.info("Updating cart item {} to quantity {} for user {}", itemId, quantity, user.getUsername());

        Cart cart = user.getCart();
        if (cart == null) {
            log.error("Cart not found for user: {}", user.getUsername());
            throw new ResourceNotFoundException("Cart not found for user: " + user.getUsername());
        }

        // Fetch item directly from repository
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        // Verify ownership
        if (!cart.equals(item.getCart())) {
            log.error("Item {} does not belong to user {}'s cart", itemId, user.getUsername());
            throw new IllegalArgumentException("Item does not belong to user's cart");
        }

        if (quantity <= 0) {
            cart.getCartItems().remove(item);
            cartItemRepository.delete(item);
            log.info("Removed cart item {} due to quantity {}", itemId, quantity);
            return null;
        }

        item.setQuantity(quantity);
        CartItem updatedItem = cartItemRepository.save(item);
        log.info("Updated cart item {} to quantity {}", itemId, quantity);
        return convertToCartItemDTO(updatedItem);
    }

    @Transactional
    public void removeItemFromCart(User user, Long itemId) {
        log.info("Removing cart item {} for user {}", itemId, user.getUsername());

        Cart cart = user.getCart();
        if (cart == null) {
            log.error("Cart not found for user: {}", user.getUsername());
            throw new ResourceNotFoundException("Cart not found for user: " + user.getUsername());
        }

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (!cart.equals(item.getCart())) {
            log.error("Item {} does not belong to user {}'s cart", itemId, user.getUsername());
            throw new IllegalArgumentException("Item does not belong to user's cart");
        }

        cart.getCartItems().remove(item);
        cartItemRepository.delete(item);
        log.info("Successfully removed cart item {}", itemId);
    }

    @Transactional
    public void clearCart(User user) {
        log.info("Clearing cart for user {}", user.getUsername());

        Cart cart = user.getCart();
        if (cart == null) {
            log.info("No cart to clear for user {}", user.getUsername());
            return;
        }

        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        log.info("Cart cleared successfully for user {}", user.getUsername());
    }

    // Helper methods to convert entities to DTOs
    private CartDTO convertToCartDTO(Cart cart) {
        return modelMapper.map(cart, CartDTO.class);
    }

    private CartItemDTO convertToCartItemDTO(CartItem cartItem) {
        return modelMapper.map(cartItem, CartItemDTO.class);
    }
}