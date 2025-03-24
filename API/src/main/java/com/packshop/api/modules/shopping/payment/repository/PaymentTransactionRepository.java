package com.packshop.api.modules.shopping.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packshop.api.modules.shopping.payment.entity.PaymentTransaction;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

  Optional<PaymentTransaction> findByPaymentId(String paymentId);

}
