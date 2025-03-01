package com.packshop.api.modules.shopping.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packshop.api.modules.shopping.entities.cart.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

}
