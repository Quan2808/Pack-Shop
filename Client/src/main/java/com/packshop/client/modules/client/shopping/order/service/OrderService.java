package com.packshop.client.modules.client.shopping.order.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.exceptions.ApiException;
import com.packshop.client.common.services.ApiBaseService;
import com.packshop.client.dto.shopping.order.OrderDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService extends ApiBaseService {

  private static final String ORDERS_API_URL = "orders";

  public OrderService(RestTemplate restTemplate, ObjectMapper objectMapper) {
    super(restTemplate, objectMapper);
  }

  public List<OrderDTO> getUserOrders(Long userId) {
    log.info("{}", getAllFromApi(ORDERS_API_URL, OrderDTO[].class));
    return getAllFromApi(ORDERS_API_URL, OrderDTO[].class);

  }

  public OrderDTO createOrder(Long addressId) {
    log.info("Creating new order via API");
    try {
      return postToApi(ORDERS_API_URL + "?address=" + addressId, null, OrderDTO.class);
    } catch (ApiException e) {
      log.error("Error creating order: {}", e.getMessage());
      throw new ApiException(
          "Unable to create your order. Please try again.",
          e.getStatusCode(),
          e.getErrorCode(),
          e.getErrors(),
          e);
    }
  }

  public OrderDTO updateOrderStatus(Long orderId, String newStatus) {
    log.info("Updating order status for orderId: {} to {}", orderId, newStatus);
    try {
      String url = ORDERS_API_URL + "/" + orderId + "/status?newStatus=" + newStatus;
      return putToApi(url, null, OrderDTO.class);
    } catch (ApiException e) {
      log.error("Error updating order {} status: {}", orderId, e.getMessage());
      if ("INVALID_STATUS".equals(e.getErrorCode())) {
        throw new ApiException(
            String.format("Cannot update order %d to '%s'. The status is not valid.", orderId, newStatus),
            e.getStatusCode(),
            e.getErrorCode(),
            e.getErrors(),
            e);
      }
      throw new ApiException(
          String.format("Failed to update order %d status to '%s'. Please try again.", orderId, newStatus),
          e.getStatusCode(),
          e.getErrorCode(),
          e.getErrors(),
          e);
    }
  }

  public void cancelOrder(Long orderId) {
    log.info("Cancelling order with orderId: {}", orderId);
    try {
      deleteFromApi(ORDERS_API_URL, orderId);
    } catch (ApiException e) {
      log.error("Error cancelling order {}: {}", orderId, e.getMessage());
      if ("INVALID_STATUS".equals(e.getErrorCode())) {
        throw new ApiException(
            String.format("Order %d cannot be cancelled due to its current status.", orderId),
            e.getStatusCode(),
            e.getErrorCode(),
            e.getErrors(),
            e);
      }
      throw new ApiException(
          String.format("Failed to cancel order %d. Please try again.", orderId),
          e.getStatusCode(),
          e.getErrorCode(),
          e.getErrors(),
          e);
    }
  }
}