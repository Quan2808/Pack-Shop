package com.packshop.api.modules.shopping.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packshop.api.modules.shopping.entities.cart.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

}
