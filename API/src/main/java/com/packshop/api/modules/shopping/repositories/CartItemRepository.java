package com.packshop.api.modules.shopping.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packshop.api.modules.shopping.entities.cart.Cart;
import com.packshop.api.modules.shopping.entities.cart.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Long productId);
}
