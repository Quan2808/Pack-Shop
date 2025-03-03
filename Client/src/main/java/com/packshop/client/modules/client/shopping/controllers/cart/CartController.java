package com.packshop.client.modules.client.shopping.controllers.cart;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.packshop.client.common.exceptions.ApiException;
import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.shopping.cart.CartDTO;
import com.packshop.client.modules.client.shopping.services.cart.CartService;

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
    public String showCart(Model model) {
        try {
            Long userId = (Long) model.getAttribute("userId");
            CartDTO cart = cartService.getCartForUser(userId);
            log.info("Cart: {}", cart);
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
            RedirectAttributes redirectAttributes) {
        try {
            cartService.addItemToCart(productId, quantity);
            redirectAttributes.addFlashAttribute("message", "Item added to cart successfully!");
            return "redirect:/cart";
        } catch (ApiException e) {
            log.error("Error adding item to cart: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add item to cart.");
            return "redirect:/cart";
        }
    }

    // @PostMapping("/update/{itemId}")
    // public String updateCartItem(
    // @PathVariable("itemId") Long itemId,
    // @RequestParam("quantity") Integer quantity,
    // RedirectAttributes redirectAttributes) {
    // try {
    // cartService.updateCartItem(itemId, quantity);
    // redirectAttributes.addFlashAttribute("message", "Cart updated
    // successfully!");
    // return "redirect:/cart";
    // } catch (ApiException e) {
    // log.error("Error updating cart item: {}", e.getMessage());
    // redirectAttributes.addFlashAttribute("errorMessage", "Failed to update
    // cart.");
    // return "redirect:/cart";
    // }
    // }

    @PostMapping("/remove/{itemId}")
    public String removeItemFromCart(
            @PathVariable("itemId") Long itemId,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.removeItemFromCart(itemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart!");
            return "redirect:/cart";
        } catch (ApiException e) {
            log.error("Error removing item from cart: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove item.");
            return "redirect:/cart";
        }
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
