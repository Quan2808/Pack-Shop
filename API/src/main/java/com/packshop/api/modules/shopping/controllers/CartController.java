package com.packshop.api.modules.shopping.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.dto.cart.CartDTO;
import com.packshop.api.modules.shopping.dto.cart.CartItemDTO;
import com.packshop.api.modules.shopping.dto.cart.CartItemRequest;
import com.packshop.api.modules.shopping.services.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO> getCart(@AuthenticationPrincipal User user) {
        CartDTO cart = cartService.getCartByUser(user);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemDTO> addItemToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CartItemRequest request) {

        // Validate request parameters
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }

        if (request.getQuantity() == null) {
            throw new IllegalArgumentException("Quantity is required");
        }

        log.info("Adding item to cart: productId={}, quantity={} for user ID: {}",
                request.getProductId(), request.getQuantity(), user.getId());

        CartItemDTO addedItem = cartService.addItemToCart(
                user, request.getProductId(), request.getQuantity());

        return ResponseEntity.status(HttpStatus.CREATED).body(addedItem);
    }

    @PutMapping("/items")
    public ResponseEntity<List<CartItemDTO>> updateCartItems(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody List<CartItemRequest> updateRequests) {

        // Validate request
        if (updateRequests == null || updateRequests.isEmpty()) {
            throw new IllegalArgumentException("Update requests cannot be empty");
        }

        for (CartItemRequest request : updateRequests) {
            if (request.getId() == null) {
                throw new IllegalArgumentException("Cart item ID is required for updates");
            }

            if (request.getQuantity() == null) {
                throw new IllegalArgumentException("Quantity is required");
            }
        }

        log.info("Updating {} cart items for user ID: {}", updateRequests.size(), user.getId());

        List<CartItemDTO> updatedItems = cartService.updateCartItems(user, updateRequests);
        return ResponseEntity.ok(updatedItems);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId) {

        // Validate path variable
        if (itemId == null || itemId <= 0) {
            log.error("Invalid item ID: {}", itemId);
            throw new IllegalArgumentException("Valid item ID is required");
        }

        log.info("Removing item {} from cart for user ID: {}", itemId, user.getId());

        cartService.removeItemFromCart(user, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal User user) {
        log.info("Clearing cart for user ID: {}", user.getId());

        cartService.clearCart(user);
        return ResponseEntity.noContent().build();
    }
}