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
    log.info("Fetching all Orders from User");
    try {
      return getAllFromApi(ORDERS_API_URL, OrderDTO[].class);
    } catch (ApiException e) {
      throw new ApiException("Failed to fetching all Orders from User", e.getStatusCode(),
          e.getErrorCode(), e.getErrors(), e);
    }
  }

  public OrderDTO createOrder() {
    log.info("Creating new order via API");
    try {
      return postToApi(ORDERS_API_URL, null, OrderDTO.class);
    } catch (ApiException e) {
      throw new ApiException("Failed to create order", e.getStatusCode(),
          e.getErrorCode(), e.getErrors(), e);
    }
  }

  public OrderDTO updateOrderStatus(Long orderId, String newStatus) {
    log.info("Updating order status for orderId: {} to {}", orderId, newStatus);
    try {
      String url = ORDERS_API_URL + "/" + orderId + "/status?newStatus=" + newStatus;
      return putToApi(url, null, OrderDTO.class);
    } catch (ApiException e) {
      throw new ApiException("Failed to update order status", e.getStatusCode(),
          e.getErrorCode(), e.getErrors(), e);
    }
  }

  public void cancelOrder(Long orderId) {
    log.info("Cancelling order with orderId: {}", orderId);
    try {
      deleteFromApi(ORDERS_API_URL, orderId);
    } catch (ApiException e) {
      if ("INVALID_STATUS".equals(e.getErrorCode()))
        throw new ApiException("Order cannot be cancelled due to invalid status.", e.getStatusCode(),
            e.getErrorCode(), e.getErrors(), e);
      throw new ApiException("Failed to cancel order", e.getStatusCode(),
          e.getErrorCode(), e.getErrors(), e);
    }
  }
}
