package com.packshop.api.modules.shopping.order.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.packshop.api.common.exceptions.ResourceNotFoundException;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.order.dto.OrderDTO;
import com.packshop.api.modules.shopping.order.entities.Order;
import com.packshop.api.modules.shopping.order.services.OrderService;
import com.packshop.api.modules.shopping.payment.PaymentTransaction;
import com.packshop.api.modules.shopping.payment.repository.PaymentTransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getUserOrders(@AuthenticationPrincipal User user) {
        log.info("Retrieving orders for user: {}", user.getUsername());
        List<OrderDTO> orders = orderService.getOrdersByUser(user);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user) {
        log.info("Retrieving order {} for user {}", orderId, user.getUsername());
        OrderDTO order = orderService.getOrderById(orderId, user);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/payment/ipn")
    public ResponseEntity<Void> handleMoMoIpn(
            @RequestParam(value = "transaction_id", required = false) String transactionId,
            @RequestBody(required = false) Map<String, Object> ipnData) {
        try {
            if (ipnData != null) {
                log.info("Received MoMo IPN with body: {}", ipnData);
                String requestId = (String) ipnData.get("requestId");
                String transId = (String) ipnData.get("transId");
                Integer resultCode = (Integer) ipnData.get("resultCode");

                PaymentTransaction transaction = paymentTransactionRepository.findByRequestId(requestId)
                        .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

                if (transId != null)
                    transaction.setTransactionId(transId);
                if (resultCode == 0) {
                    transaction.setStatus(PaymentTransaction.PaymentStatus.SUCCESS);
                } else {
                    transaction.setStatus(PaymentTransaction.PaymentStatus.FAILED);
                }
                paymentTransactionRepository.save(transaction);
                log.info("IPN processed from body for requestId: {}", requestId);
            } else if (transactionId != null) {
                log.info("Received MoMo IPN with transaction_id: {}", transactionId);
                PaymentTransaction transaction = paymentTransactionRepository.findByTransactionId(transactionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
                transaction.setStatus(PaymentTransaction.PaymentStatus.SUCCESS); // Mặc định SUCCESS khi test thủ công
                paymentTransactionRepository.save(transaction);
                log.info("IPN processed from query param for transactionId: {}", transactionId);
            } else {
                log.warn("No valid IPN data provided");
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            log.error("Error processing MoMo IPN: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @RequestParam("address") Long addressId,
            @AuthenticationPrincipal User user) {
        log.info("Creating order for user: {}", user.getUsername());
        // OrderDTO createdOrder = orderService.createOrderFromCart(user, addressId);
        OrderDTO createdOrder = orderService.createOrderFromCartWithMoMo(user, addressId);
        return ResponseEntity.ok(createdOrder);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.Status newStatus,
            @AuthenticationPrincipal User user) {
        log.info("Updating order {} status to {} for user {}",
                orderId, newStatus, user.getUsername());
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, newStatus, user);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user) {
        log.info("Cancelling order {} for user {}", orderId, user.getUsername());
        orderService.cancelOrder(orderId, user);
        return ResponseEntity.noContent().build();
    }
}
