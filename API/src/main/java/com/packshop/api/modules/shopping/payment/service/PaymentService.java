package com.packshop.api.modules.shopping.payment.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.packshop.api.common.exceptions.ResourceNotFoundException;
import com.packshop.api.modules.shopping.order.entities.Order;
import com.packshop.api.modules.shopping.order.repositories.OrderRepository;
import com.packshop.api.modules.shopping.payment.dto.PaymentTransactionDTO;
import com.packshop.api.modules.shopping.payment.entity.PaymentTransaction;
import com.packshop.api.modules.shopping.payment.repository.PaymentTransactionRepository;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
  private final OrderRepository orderRepository;
  private final PaymentTransactionRepository paymentTransactionRepository;
  private final ModelMapper modelMapper;

  @Value("${paypal.client.id}")
  private String clientId;

  @Value("${paypal.client.secret}")
  private String clientSecret;

  @Value("${paypal.mode}")
  private String mode;

  private APIContext apiContext;

  @PostConstruct
  public void init() {
    this.apiContext = new APIContext(clientId, clientSecret, mode);
  }

  @Transactional(rollbackOn = Exception.class)
  public PaymentTransactionDTO createPayment(Long orderId, Long payerId, String returnUrl, String cancelUrl) {
    log.info("Creating payment for order ID: {}", orderId);
    Objects.requireNonNull(orderId, "Order ID must not be null");
    Objects.requireNonNull(returnUrl, "Return URL must not be null");
    Objects.requireNonNull(cancelUrl, "Cancel URL must not be null");

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    try {
      Payment createdPayment = createPayPalPayment(order, returnUrl, cancelUrl);
      log.debug("PayPal payment created with ID: {}", createdPayment.getId());

      PaymentTransaction paymentTransaction = new PaymentTransaction();
      paymentTransaction.setOrder(order);
      paymentTransaction.setPayerId(Long.toString(payerId));
      paymentTransaction.setPaymentId(createdPayment.getId());
      paymentTransaction.setStatus(PaymentTransaction.PaymentStatus.CREATED);
      paymentTransaction.setAmount(order.getTotalAmount());
      paymentTransaction.setTransactionDate(LocalDateTime.now());
      paymentTransactionRepository.save(paymentTransaction);

      order.setPaymentTransaction(paymentTransaction);
      orderRepository.save(order);

      log.info("Payment transaction saved for order ID: {}", orderId);
      return toDTO(paymentTransaction);
    } catch (PayPalRESTException e) {
      log.error("Failed to create payment for order ID: {}", orderId, e);
      throw new RuntimeException("Failed to create PayPal payment", e);
    }
  }

  @Transactional(rollbackOn = Exception.class)
  public PaymentTransactionDTO executePayment(String paymentId, String payerId) {
    log.info("Executing payment with ID: {}", paymentId);
    Objects.requireNonNull(paymentId, "Payment ID must not be null");
    Objects.requireNonNull(payerId, "Payer ID must not be null");

    Payment payment = new Payment();
    payment.setId(paymentId);

    try {
      PaymentExecution paymentExecution = new PaymentExecution();
      paymentExecution.setPayerId(payerId);
      Payment executedPayment = payment.execute(apiContext, paymentExecution);

      PaymentTransaction paymentTransaction = paymentTransactionRepository
          .findByPaymentId(paymentId)
          .orElseThrow(() -> new RuntimeException(paymentId));

      paymentTransaction.setPayerId(payerId);
      paymentTransaction.setStatus(PaymentTransaction.PaymentStatus.COMPLETED);
      paymentTransactionRepository.save(paymentTransaction);

      Order order = paymentTransaction.getOrder();
      order.setStatus(Order.Status.PENDING);
      orderRepository.save(order);

      log.info("Payment executed successfully for payment ID: {}", paymentId);
      return toDTO(paymentTransaction);
    } catch (PayPalRESTException e) {
      log.error("Failed to execute payment with ID: {}", paymentId, e);
      throw new RuntimeException("Failed to execute PayPal payment", e);
    }
  }

  private Payment createPayPalPayment(Order order, String returnUrl, String cancelUrl)
      throws PayPalRESTException {
    Amount amount = new Amount();
    amount.setCurrency("USD");
    amount.setTotal(String.format("%.2f", order.getTotalAmount() / 100.0));

    Transaction transaction = new Transaction();
    transaction.setAmount(amount);
    transaction.setDescription("Order #" + order.getId());

    List<Transaction> transactions = new ArrayList<>();
    transactions.add(transaction);

    Payer payer = new Payer();
    payer.setPaymentMethod("paypal");

    Payment payment = new Payment();
    payment.setIntent("sale");
    payment.setPayer(payer);
    payment.setTransactions(transactions);

    RedirectUrls redirectUrls = new RedirectUrls();
    redirectUrls.setCancelUrl(cancelUrl);
    redirectUrls.setReturnUrl(returnUrl);
    payment.setRedirectUrls(redirectUrls);

    return payment.create(apiContext);
  }

  private PaymentTransactionDTO toDTO(PaymentTransaction paymentTransaction) {
    return modelMapper.map(paymentTransaction, PaymentTransactionDTO.class);
  }
}