package com.packshop.api.modules.shopping.payment.service;

import com.packshop.api.modules.shopping.order.entities.Order;
import com.packshop.api.modules.shopping.payment.PaymentTransaction;

public interface PaymentService {
  PaymentTransaction createPaymentTransaction(Order order);
}