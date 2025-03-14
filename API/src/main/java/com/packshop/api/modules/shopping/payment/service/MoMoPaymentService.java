package com.packshop.api.modules.shopping.payment.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.packshop.api.modules.shopping.order.entities.Order;
import com.packshop.api.modules.shopping.payment.PaymentTransaction;
import com.packshop.api.modules.shopping.payment.PaymentTransactionDTO;
import com.packshop.api.modules.shopping.payment.repository.PaymentTransactionRepository;
import com.packshop.api.modules.shopping.payment.utility.MoMoSignatureGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoMoPaymentService implements PaymentService {

  private final MoMoSignatureGenerator signatureGenerator;
  private final PaymentTransactionRepository paymentTransactionRepository;
  private final RestTemplate restTemplate;

  @Value("${momo.partner-code}")
  private String partnerCode;

  @Value("${momo.api-endpoint}")
  private String apiEndpoint;

  @Value("${momo.redirect-url}")
  private String redirectUrl;

  @Value("${momo.ipn-url}")
  private String ipnUrl;

  @Override
  public PaymentTransaction createPaymentTransaction(Order order) {
    return null;
    // String requestId = "REQ_" + System.currentTimeMillis();
    // String orderId = "ORDER_" + order.getId() + "_" + System.currentTimeMillis();
    // String signature = signatureGenerator.generateSignature(requestId, orderId,
    // order.getTotalAmount(),
    // new HashMap<>());

    // // Tạo payload cho API MoMo Sandbox
    // Map<String, Object> requestBody = new HashMap<>();
    // requestBody.put("partnerCode", partnerCode);
    // requestBody.put("requestId", requestId);
    // requestBody.put("orderId", orderId);
    // requestBody.put("amount", order.getTotalAmount());
    // requestBody.put("orderInfo", "Payment for order #" + order.getId() + "
    // (Sandbox Test)");
    // requestBody.put("redirectUrl", redirectUrl);
    // requestBody.put("ipnUrl", ipnUrl);
    // requestBody.put("requestType", "captureWallet");
    // requestBody.put("extraData", "");
    // requestBody.put("signature", signature);
    // requestBody.put("lang", "vi");

    // log.info("MoMo Request Payload: {}", requestBody);

    // HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.APPLICATION_JSON);
    // HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody,
    // headers);

    // try {
    // ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
    // apiEndpoint,
    // HttpMethod.POST,
    // entity,
    // new ParameterizedTypeReference<Map<String, Object>>() {
    // });
    // Map<String, Object> responseBody = response.getBody();

    // if (responseBody == null || !response.getStatusCode().is2xxSuccessful()) {
    // throw new RuntimeException("Failed to create MoMo payment transaction: " +
    // response.getStatusCode());
    // }

    // log.info("MoMo Response: {}", responseBody);

    // // Tạo PaymentTransactionDTO bằng Builder
    // PaymentTransactionDTO dto = PaymentTransactionDTO.builder()
    // .orderId(order.getId())
    // .transactionId((String) responseBody.get("orderId"))
    // .requestId(requestId)
    // .partnerCode(partnerCode)
    // .amount(order.getTotalAmount())
    // .status(PaymentTransaction.PaymentStatus.PENDING.name()) // Chuyển enum sang
    // String
    // .payUrl((String) responseBody.get("payUrl"))
    // .qrCodeUrl((String) responseBody.get("qrCodeUrl"))
    // .transactionDate(LocalDateTime.now())
    // .responseMessage((String) responseBody.get("message"))
    // .build();

    // PaymentTransaction paymentTransaction = fromDTO(dto, order);
    // return paymentTransactionRepository.save(paymentTransaction);

    // } catch (Exception e) {
    // log.error("Error calling MoMo Sandbox API: {}", e.getMessage());
    // throw new RuntimeException("Failed to integrate with MoMo Sandbox", e);
    // }
  }

  public static PaymentTransaction fromDTO(PaymentTransactionDTO dto, Order order) {
    PaymentTransaction pt = new PaymentTransaction();
    pt.setOrder(order);
    pt.setTransactionId(dto.getTransactionId());
    pt.setRequestId(dto.getRequestId());
    pt.setPartnerCode(dto.getPartnerCode());
    pt.setAmount(dto.getAmount());
    pt.setStatus(PaymentTransaction.PaymentStatus.valueOf(dto.getStatus()));
    pt.setPayUrl(dto.getPayUrl());
    pt.setQrCodeUrl(dto.getQrCodeUrl());
    pt.setTransactionDate(dto.getTransactionDate());
    pt.setResponseMessage(dto.getResponseMessage());
    return pt;
  }
}