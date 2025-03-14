package com.packshop.api.modules.shopping.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.packshop.api.modules.shopping.payment.PaymentTransaction;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

  Optional<PaymentTransaction> findByRequestId(String requestId);

  Optional<PaymentTransaction> findByTransactionId(String transactionId);

}
