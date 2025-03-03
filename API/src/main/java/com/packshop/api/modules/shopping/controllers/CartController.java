package com.packshop.api.modules.shopping.controllers;

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
        log.info("Fetching cart for user: {}", user.getUsername());
        CartDTO cart = cartService.getCartByUser(user);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemDTO> addItemToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CartItemRequest request) {
        log.info("Adding item to cart: productId={}, quantity={}", request.getProductId(), request.getQuantity());
        CartItemDTO addedItem = cartService.addItemToCart(user, request.getProductId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(addedItem);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartItemDTO> updateCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemRequest request) {
        log.info("Updating cart item: itemId={}, quantity={}", itemId, request.getQuantity());
        CartItemDTO updatedItem = cartService.updateCartItem(user, itemId, request.getQuantity());
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId) {
        log.info("Removing item from cart: itemId={}", itemId);
        cartService.removeItemFromCart(user, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal User user) {
        log.info("Clearing cart for user: {}", user.getUsername());
        cartService.clearCart(user);
        return ResponseEntity.noContent().build();
    }
}