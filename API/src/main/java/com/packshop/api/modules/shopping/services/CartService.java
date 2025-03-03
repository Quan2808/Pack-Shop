package com.packshop.api.modules.shopping.services;

import java.util.Optional;

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

        // Validate product
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Cart cart = user.getCart() != null ? user.getCart() : createCart(user);

        // Check if item already exists
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, productId);
        if (existingItem.isPresent()) {
            log.warn("Item with productId {} already exists in cart", productId);
            return convertToCartItemDTO(existingItem.get());
            // throw new IllegalStateException("Product " + productId + " already exists in
            // cart. Use update instead.");
        }

        // Create new item
        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProduct(productId);
        newItem.setQuantity(quantity);
        cart.addCartItem(newItem);

        CartItem savedItem = cartItemRepository.save(newItem);
        return convertToCartItemDTO(savedItem);
    }

    @Transactional
    public CartItemDTO updateCartItem(User user, Long itemId, int quantity) {
        Cart cart = user.getCart();
        if (cart == null) {
            throw new ResourceNotFoundException("Cart not found for user: " + user.getUsername());
        }

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (!cart.equals(item.getCart())) {
            throw new IllegalArgumentException("Item does not belong to user's cart");
        }

        item.setQuantity(quantity);
        CartItem updatedItem = cartItemRepository.save(item);
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