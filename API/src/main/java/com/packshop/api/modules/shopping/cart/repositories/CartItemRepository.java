package com.packshop.api.modules.shopping.cart.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packshop.api.modules.shopping.cart.entities.Cart;
import com.packshop.api.modules.shopping.cart.entities.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Long productId);

    void deleteByCart(Cart cart);
}
