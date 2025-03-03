package com.packshop.client.modules.client.shopping.services.cart;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        log.info("Fetching cart for current user");
        return getFromApi(CART_API_URL, userId, CartDTO.class);
    }

    public CartItemDTO addItemToCart(Long productId, Integer quantity) {
        log.info("Adding item to cart: productId={}, quantity={}", productId, quantity);
        CartItemRequest request = new CartItemRequest(productId, quantity);
        return postToApi(CART_API_URL + "/items", request, CartItemDTO.class);
    }

    // public CartItemDTO updateCartItem(Long itemId, Integer quantity) {
    // log.info("Updating cart item: itemId={}, quantity={}", itemId, quantity);
    // CartItemRequest request = new CartItemRequest(null, quantity); // productId
    // không cần thiết khi update
    // return putToApi(CART_API_URL + "/items", request, itemId, CartItemDTO.class);
    // }

    public void removeItemFromCart(Long itemId) {
        log.info("Removing item from cart: itemId={}", itemId);
        deleteFromApi(CART_API_URL + "/items", itemId);
    }

    public void clearCart() {
        log.info("Clearing cart for current user");
        deleteFromApi(CART_API_URL + "/clear", null);
    }
}
