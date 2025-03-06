package com.packshop.client.modules.client.shopping.cart.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.exceptions.ApiException;
import com.packshop.client.common.services.ApiBaseService;
import com.packshop.client.dto.shopping.cart.CartDTO;
import com.packshop.client.dto.shopping.cart.CartItemDTO;
import com.packshop.client.dto.shopping.cart.CartItemRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CartService extends ApiBaseService {

    private static final String CART_API_URL = "carts";

    public CartService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    public CartDTO getCartForUser(Long userId) {
        return getFromApi(CART_API_URL, userId, CartDTO.class);
    }

    public CartItemDTO addItemToCart(Long productId, Integer quantity) {
        try {
            log.info("Adding item to cart: productId={}, quantity={}", productId, quantity);
            CartItemRequest request = new CartItemRequest(null, productId, quantity);
            return postToApi(CART_API_URL + "/items", request, CartItemDTO.class);
        } catch (ApiException e) {
            if ("Insufficient".contains(e.getMessage())) {
                throw new ApiException("Not enough stock available", e.getStatusCode(),
                        e.getErrorCode(), e.getErrors(), e);
            } else if ("INTERNAL_ERROR".equals(e.getErrorCode())) {
                throw new ApiException("An unexpected error occurred while adding product to cart",
                        e.getStatusCode(), e.getErrorCode(), e.getErrors(), e);
            }
            throw new ApiException("Failed to add product to cart", e.getStatusCode(),
                    e.getErrorCode(), e.getErrors(), e);
        }
    }

    public List<CartItemDTO> updateCartItems(List<CartItemRequest> updateRequests) {
        try {
            return putToApiMultiple(CART_API_URL + "/items", updateRequests, CartItemDTO.class);
        } catch (ApiException e) {
            // Handle insufficient stock scenario
            if (e.getErrorCode() != null && e.getErrorCode().contains("INSUFFICIENT_STOCK")) {
                throw new ApiException(
                    "Not enough stock available",
                    e.getStatusCode(),
                    e.getErrorCode(),
                    e.getErrors(),
                    e
                );
            }
            // Handle invalid status or other specific errors
            else if ("INVALID_STATUS".equals(e.getErrorCode())) {
                throw new ApiException(
                    "Cannot update cart items due to invalid status",
                    e.getStatusCode(),
                    e.getErrorCode(),
                    e.getErrors(),
                    e
                );
            }
            // Handle internal server errors
            else if ("INTERNAL_ERROR".equals(e.getErrorCode()) || e.getStatusCode() == 500) {
                throw new ApiException(
                    "An unexpected error occurred while updating cart",
                    e.getStatusCode(),
                    e.getErrorCode(),
                    e.getErrors(),
                    e
                );
            }
            // Generic fallback
            throw new ApiException(
                "Failed to update cart items: " + e.getMessage(),
                e.getStatusCode(),
                e.getErrorCode(),
                e.getErrors(),
                e
            );
        }
    }

    public void removeItemFromCart(Long itemId) {
        log.info("Removing item from cart: itemId={}", itemId);
        deleteFromApi(CART_API_URL + "/items", itemId);
    }

    public void clearCart() {
        log.info("Clearing cart for current user");
        deleteFromApi(CART_API_URL + "/clear", null);
    }
}
