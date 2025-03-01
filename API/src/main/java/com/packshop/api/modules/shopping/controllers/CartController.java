package com.packshop.api.modules.shopping.controllers;

import java.util.HashMap;
import java.util.Map;

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
import com.packshop.api.modules.shopping.entities.cart.Cart;
import com.packshop.api.modules.shopping.entities.cart.CartItem;
import com.packshop.api.modules.shopping.services.CartService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal User user) {
        Cart cart = cartService.getCartByUser(user);
        return ResponseEntity.ok(cart);
    }

    static class AddItemRequest {
        @Positive(message = "Product ID must be positive")
        private Long productId;

        @Positive(message = "Quantity must be greater than 0")
        private int quantity;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    @PostMapping("/items")
    public ResponseEntity<CartItem> addItemToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddItemRequest request) {
        CartItem addedItem = cartService.addItemToCart(user, request.getProductId(), request.getQuantity());
        return new ResponseEntity<>(addedItem, HttpStatus.CREATED);
    }

    static class UpdateItemRequest {
        @Positive(message = "Quantity must be greater than 0")
        private int quantity;

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemRequest request) {

        CartItem updatedItem = cartService.updateCartItem(user, itemId, request.getQuantity());

        if (updatedItem == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Item removed from cart");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Map<String, String>> removeItemFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId) {

        cartService.removeItemFromCart(user, itemId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Item removed from cart");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Cart cleared successfully");
        return ResponseEntity.ok(response);
    }
}