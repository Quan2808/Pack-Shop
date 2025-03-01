package com.packshop.api.modules.shopping.services;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packshop.api.modules.catalog.repositories.ProductRepository;
import com.packshop.api.modules.identity.entities.User;
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

    @Transactional(readOnly = true)
    public Cart getCartByUser(User user) {
        return user.getCart() != null ? user.getCart() : createCart(user);
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
    public CartItem addItemToCart(User user, Long productId, int quantity) {
        // Verify product exists
        boolean productExists = productRepository.existsById(productId);
        if (!productExists) {
            throw new RuntimeException("Product not found with id: " + productId);
        }

        // Get or create cart
        Cart cart = getCartByUser(user);

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity of existing item
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartItemRepository.save(item);
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(productId);
            newItem.setQuantity(quantity);

            cart.getCartItems().add(newItem);
            cartRepository.save(cart);
            return newItem;
        }
    }

    @Transactional
    public CartItem updateCartItem(User user, Long itemId, int quantity) {
        Cart cart = getCartByUser(user);

        // Find the cart item
        Optional<CartItem> optionalItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();

        if (optionalItem.isEmpty()) {
            throw new RuntimeException("Cart item not found with id: " + itemId);
        }

        CartItem item = optionalItem.get();

        if (quantity <= 0) {
            // Remove item if quantity is 0 or less
            cart.getCartItems().remove(item);
            cartItemRepository.delete(item);
            cartRepository.save(cart);
            return null;
        } else {
            // Update quantity
            item.setQuantity(quantity);
            return cartItemRepository.save(item);
        }
    }

    @Transactional
    public void removeItemFromCart(User user, Long itemId) {
        Cart cart = getCartByUser(user);

        // Find the cart item
        Optional<CartItem> optionalItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();

        if (optionalItem.isEmpty()) {
            throw new RuntimeException("Cart item not found with id: " + itemId);
        }

        CartItem item = optionalItem.get();
        cart.getCartItems().remove(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getCartByUser(user);
        Set<CartItem> items = cart.getCartItems();

        // Clear items
        cartItemRepository.deleteAll(items);
        items.clear();
        cartRepository.save(cart);
    }
}