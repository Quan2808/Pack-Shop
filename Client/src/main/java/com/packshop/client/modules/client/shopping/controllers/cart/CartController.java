package com.packshop.client.modules.client.shopping.controllers.cart;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.packshop.client.common.exceptions.ApiException;
import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.shopping.cart.CartDTO;
import com.packshop.client.dto.shopping.cart.CartItemRequest;
import com.packshop.client.modules.client.shopping.services.cart.CartService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final ViewRenderer viewRenderer;
    private final CartService cartService;

    @GetMapping
    public String showCart(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return "redirect:/account/authentication";
        }
        try {
            Long userId = (Long) model.getAttribute("userId");
            CartDTO cart = cartService.getCartForUser(userId);
            model.addAttribute("cart", cart);
        } catch (ApiException e) {
            log.error("Error fetching cart: {}", e.getMessage());
            model.addAttribute("errorMessage", "Unable to load cart. Please try again later.");
        }
        return viewRenderer.renderView(model, "client/cart/index", "Cart");
    }

    @PostMapping("/add")
    public String addItemToCart(
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") Integer quantity,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return "redirect:/account/authentication";
        }

        try {
            Long userId = (Long) session.getAttribute("userId");

            CartDTO cart = cartService.getCartForUser(userId);
            boolean productExists = cart.getCartItems().stream()
                    .anyMatch(item -> item.getProduct().getId().equals(productId));

            if (productExists) {
                log.info("Product {} already exists in cart for user {}", productId, userId);
                redirectAttributes.addFlashAttribute("errorMessage", "Product is already in your cart!");
                return "redirect:" + (referer != null ? referer : "/products/" + productId);
            }

            cartService.addItemToCart(productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to cart successfully!");
            return "redirect:/cart";
        } catch (ApiException e) {
            log.error("Error adding item to cart: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add item to cart.");
            return "redirect:/cart";
        }
    }

    @PostMapping("/items")
    public String updateCartItems(@RequestParam("id") List<Long> ids,
            @RequestParam("quantity") List<Integer> quantities,
            RedirectAttributes redirectAttributes) {

        try {
            // Validate input lists have the same size
            if (ids.size() != quantities.size()) {
                throw new IllegalArgumentException("Number of item IDs must match number of quantities");
            }

            List<CartItemRequest> updateRequests = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) {
                updateRequests.add(new CartItemRequest(ids.get(i), null, quantities.get(i)));
            }

            cartService.updateCartItems(updateRequests);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated successfully");
        } catch (ApiException | IllegalArgumentException e) {
            log.error("Error updating cart items: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove/{itemId}")
    public String removeItemFromCart(
            @PathVariable("itemId") Long itemId,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.removeItemFromCart(itemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart!");
        } catch (ApiException e) {
            log.error("Error removing item from cart: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove item.");
        }
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        try {
            cartService.clearCart();
            redirectAttributes.addFlashAttribute("message", "Cart cleared successfully!");
            return "redirect:/cart";
        } catch (ApiException e) {
            log.error("Error clearing cart: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to clear cart.");
            return "redirect:/cart";
        }
    }
}
