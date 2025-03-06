package com.packshop.api.modules.shopping.cart.service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packshop.api.common.exceptions.InsufficientStockException;
import com.packshop.api.common.exceptions.ResourceNotFoundException;
import com.packshop.api.modules.catalog.entities.product.Product;
import com.packshop.api.modules.catalog.repositories.ProductRepository;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.cart.dto.CartDTO;
import com.packshop.api.modules.shopping.cart.dto.CartItemDTO;
import com.packshop.api.modules.shopping.cart.dto.CartItemRequest;
import com.packshop.api.modules.shopping.cart.entities.Cart;
import com.packshop.api.modules.shopping.cart.entities.CartItem;
import com.packshop.api.modules.shopping.cart.repositories.CartItemRepository;
import com.packshop.api.modules.shopping.cart.repositories.CartRepository;
import com.packshop.api.modules.shopping.dto.ProductItemDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "carts", key = "#user.id")
    public CartDTO getCartByUser(User user) {
        Cart cart = ensureCartExists(user);
        return convertToCartDTO(cart);
    }

    @Transactional
    public Cart ensureCartExists(User user) {
        if (user.getCart() == null) {
            return createCart(user);
        }
        return user.getCart();
    }

    @Transactional
    public Cart createCart(User user) {
        if (user.getCart() != null) {
            log.debug("Cart already exists for user: {}", user.getId());
            return user.getCart();
        }

        log.debug("Creating new cart for user: {}", user.getId());
        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);
        return cartRepository.save(cart);
    }

    @Transactional
    @CacheEvict(value = "carts", key = "#user.id")
    public CartItemDTO addItemToCart(User user, Long productId, int quantity) {
        log.info("Adding product {} with quantity {} to cart for user ID: {}",
                productId, quantity, user.getId());

        // Validate product and quantity
        validatePositiveQuantity(quantity, productId);
        validateProductAvailability(productId, quantity);

        // Get or create cart
        Cart cart = ensureCartExists(user);

        // Check if item already exists
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndProduct(cart, productId);

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + quantity;

            // Validate the new total quantity
            validateProductAvailability(productId, newQuantity);

            // Update quantity
            existingItem.setQuantity(newQuantity);
            CartItem savedItem = cartItemRepository.save(existingItem);
            log.info("Updated existing cart item for product {} to quantity {}", productId, newQuantity);
            return convertToCartItemDTO(savedItem);
        }

        // Create new cart item
        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProduct(productId);
        newItem.setQuantity(quantity);
        cart.addCartItem(newItem);

        CartItem savedItem = cartItemRepository.save(newItem);
        log.info("Added new cart item with ID: {}", savedItem.getId());
        return convertToCartItemDTO(savedItem);
    }

    @Transactional
    @CacheEvict(value = "carts", key = "#user.id")
    public List<CartItemDTO> updateCartItems(User user, List<CartItemRequest> updateRequests) {
        Cart cart = user.getCart();
        if (cart == null) {
            log.error("Cart not found for user ID: {}", user.getId());
            throw new ResourceNotFoundException("Cart not found");
        }

        // Create a map of cart items by ID for efficient lookup
        Map<Long, CartItem> currentItemMap = cart.getCartItems().stream()
                .collect(Collectors.toMap(CartItem::getId, Function.identity()));

        // Process all update requests
        List<CartItem> itemsToUpdate = new ArrayList<>();

        for (CartItemRequest request : updateRequests) {
            if (request.getId() == null) {
                log.error("Cart item ID is null in update request");
                throw new IllegalArgumentException("Cart item ID cannot be null");
            }

            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                log.error("Invalid quantity {} for cart item {}", request.getQuantity(), request.getId());
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }

            CartItem item = currentItemMap.get(request.getId());
            if (item == null) {
                log.error("Cart item not found with ID: {}", request.getId());
                throw new ResourceNotFoundException("Cart item not found with ID: " + request.getId());
            }

            if (!cart.equals(item.getCart())) {
                log.error("Security violation: Item {} does not belong to user's cart", request.getId());
                throw new AccessDeniedException("You do not have permission to modify this cart item");
            }

            // Validate product availability
            validateProductAvailability(item.getProduct(), request.getQuantity());

            // Update quantity
            item.setQuantity(request.getQuantity());
            itemsToUpdate.add(item);
        }

        // Save all updated items in a single operation
        List<CartItem> savedItems = cartItemRepository.saveAll(itemsToUpdate);
        log.info("Successfully updated {} cart items", savedItems.size());

        return savedItems.stream()
                .map(this::convertToCartItemDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "carts", key = "#user.id")
    public void removeItemFromCart(User user, Long itemId) {
        Cart cart = user.getCart();
        if (cart == null) {
            log.error("Cart not found for user ID: {}", user.getId());
            throw new ResourceNotFoundException("Cart not found");
        }

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Cart item not found with ID: {}", itemId);
                    return new ResourceNotFoundException("Cart item not found");
                });

        if (!cart.equals(item.getCart())) {
            log.error("Security violation: Item {} does not belong to user's cart", itemId);
            throw new AccessDeniedException("You do not have permission to remove this cart item");
        }

        cart.getCartItems().remove(item);
        cartItemRepository.delete(item);
        log.info("Successfully removed cart item {}", itemId);
    }

    @Transactional
    @CacheEvict(value = "carts", key = "#user.id")
    public void clearCart(User user) {
        Cart cart = user.getCart();
        if (cart == null) {
            log.info("No cart to clear for user ID: {}", user.getId());
            return;
        }

        // Use a repository method to delete by cart instead of collection manipulation
        cartItemRepository.deleteByCart(cart);

        // Clear the collection to keep the in-memory state consistent
        cart.getCartItems().clear();

        log.info("Cart cleared successfully for user ID: {}", user.getId());
    }

    // Helper methods to convert entities to DTOs
    private CartDTO convertToCartDTO(Cart cart) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cartDTO.setTotalItems(cart.getCartItems().size());

        List<Long> productIds = cart.getCartItems().stream()
                .map(CartItem::getProduct)
                .collect(Collectors.toList());
        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<CartItemDTO> cartItemDTOs = cart.getCartItems().stream()
                .map(cartItem -> {
                    CartItemDTO dto = modelMapper.map(cartItem, CartItemDTO.class);
                    Product product = productMap.get(cartItem.getProduct());
                    if (product == null) {
                        throw new ResourceNotFoundException("Product not found with id: " + cartItem.getProduct());
                    }
                    ProductItemDTO productDTO = modelMapper.map(product, ProductItemDTO.class);
                    dto.setProduct(productDTO);
                    return dto;
                })
                .collect(Collectors.toList());

        cartDTO.setCartItems(cartItemDTOs);
        return cartDTO;
    }

    private CartItemDTO convertToCartItemDTO(CartItem cartItem) {
        CartItemDTO dto = modelMapper.map(cartItem, CartItemDTO.class);
        Product product = productRepository.findById(cartItem.getProduct())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product not found with id: " + cartItem.getProduct()));
        ProductItemDTO productDTO = modelMapper.map(product, ProductItemDTO.class);
        dto.setProduct(productDTO);
        return dto;
    }

    public Product checkProductAvailability(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative for product: " + productId);
        }

        if (product.getQuantity() < quantity || product.getQuantity() <= 0) {
            throw new InsufficientStockException("Insufficient stock for product: " + productId);
        }
        return product;
    }

    private Product validateProductAvailability(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", productId);
                    return new ResourceNotFoundException("Product not found");
                });

        validatePositiveQuantity(quantity, productId);

        if (product.getQuantity() < quantity) {
            log.error("Insufficient stock for product {}: requested={}, available={}",
                    productId, quantity, product.getQuantity());
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        return product;
    }

    private void validatePositiveQuantity(int quantity, Long productId) {
        if (quantity <= 0) {
            log.error("Invalid quantity {} for product {}", quantity, productId);
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }
}