package com.packshop.api.modules.shopping.services;

import java.util.Optional;
import java.util.Set;

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
        if (user.getCart() != null) {
            return user.getCart();
        }

        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);
        return cartRepository.save(cart);
    }

    @Transactional
    public CartItemDTO addItemToCart(User user, Long productId, int quantity) {
        log.info("Adding product {} with quantity {} to user {}'s cart", productId, quantity, user.getUsername());

        // Verify product exists
        if (!productRepository.existsById(productId)) {
            log.error("Product not found with id: {}", productId);
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        // Validate quantity
        if (quantity <= 0) {
            log.error("Invalid quantity: {} for product {}", quantity, productId);
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // Get or create cart
        Cart cart = user.getCart();
        if (cart == null) {
            cart = createCart(user);
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().equals(productId))
                .findFirst();

        CartItem resultItem;
        if (existingItem.isPresent()) {
            // Update quantity of existing item
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            resultItem = cartItemRepository.save(item);
            log.info("Updated existing cart item, new quantity: {}", item.getQuantity());
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(productId);
            newItem.setQuantity(quantity);

            // Use the helper method in Cart entity to maintain relationship
            cart.addCartItem(newItem);

            // Save the cart item directly, let JPA handle the relationship
            resultItem = cartItemRepository.save(newItem);
            log.info("Created new cart item with id: {}", resultItem.getId());
        }

        return convertToCartItemDTO(resultItem);
    }

    @Transactional
    public CartItemDTO updateCartItem(User user, Long itemId, int quantity) {
        log.info("Updating cart item {} to quantity {} for user {}", itemId, quantity, user.getUsername());

        Cart cart = user.getCart();
        if (cart == null) {
            log.error("User {} does not have a cart", user.getUsername());
            throw new ResourceNotFoundException("Cart not found for user: " + user.getUsername());
        }

        // Find the cart item
        CartItem item = cart.getCartItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Cart item not found with id: {}", itemId);
                    return new ResourceNotFoundException("Cart item not found with id: " + itemId);
                });

        if (quantity <= 0) {
            // Remove item if quantity is 0 or less
            cart.getCartItems().remove(item);
            cartItemRepository.delete(item);
            cartRepository.save(cart);
            log.info("Removed cart item {} because quantity was {}", itemId, quantity);
            return null;
        } else {
            // Update quantity
            item.setQuantity(quantity);
            CartItem updatedItem = cartItemRepository.save(item);
            log.info("Updated cart item {} to quantity {}", itemId, quantity);
            return convertToCartItemDTO(updatedItem);
        }
    }

    @Transactional
    public void removeItemFromCart(User user, Long itemId) {
        log.info("Removing cart item {} for user {}", itemId, user.getUsername());

        Cart cart = user.getCart();
        if (cart == null) {
            log.error("User {} does not have a cart", user.getUsername());
            throw new ResourceNotFoundException("Cart not found for user: " + user.getUsername());
        }

        // Find the cart item
        CartItem item = cart.getCartItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Cart item not found with id: {}", itemId);
                    return new ResourceNotFoundException("Cart item not found with id: " + itemId);
                });

        cart.getCartItems().remove(item);
        cartItemRepository.delete(item);
        log.info("Successfully removed cart item {}", itemId);
    }

    @Transactional
    public void clearCart(User user) {
        log.info("Clearing cart for user {}", user.getUsername());

        Cart cart = user.getCart();
        if (cart == null) {
            log.info("User {} did not have a cart to clear", user.getUsername());
            return;
        }

        Set<CartItem> items = cart.getCartItems();

        // Clear items
        cartItemRepository.deleteAll(items);
        items.clear();
        cartRepository.save(cart);
        log.info("Cart cleared successfully for user {}", user.getUsername());
    }

    // Helper methods to convert entities to DTOs
    private CartDTO convertToCartDTO(Cart cart) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        return cartDTO;
    }

    private CartItemDTO convertToCartItemDTO(CartItem cartItem) {
        CartItemDTO cartItemDTO = modelMapper.map(cartItem, CartItemDTO.class);
        return cartItemDTO;
    }
}