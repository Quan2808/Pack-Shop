package com.packshop.api.modules.shopping.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.entities.cart.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);
}
