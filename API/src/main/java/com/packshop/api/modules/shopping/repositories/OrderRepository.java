package com.packshop.api.modules.shopping.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.entities.order.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);

    Optional<Order> findByIdAndUser(Long orderId, User user);

    long countByUserAndStatus(User user, Order.Status status);
}
