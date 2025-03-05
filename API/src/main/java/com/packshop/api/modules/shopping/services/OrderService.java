package com.packshop.api.modules.shopping.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packshop.api.common.exceptions.InsufficientStockException;
import com.packshop.api.common.exceptions.ResourceNotFoundException;
import com.packshop.api.modules.catalog.entities.product.Product;
import com.packshop.api.modules.catalog.repositories.ProductRepository;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.dto.ProductItemDTO;
import com.packshop.api.modules.shopping.dto.order.OrderDTO;
import com.packshop.api.modules.shopping.dto.order.OrderItemDTO;
import com.packshop.api.modules.shopping.entities.cart.Cart;
import com.packshop.api.modules.shopping.entities.order.Order;
import com.packshop.api.modules.shopping.entities.order.OrderItem;
import com.packshop.api.modules.shopping.repositories.CartRepository;
import com.packshop.api.modules.shopping.repositories.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public OrderDTO createOrderFromCart(User user) {
        log.info("Creating order from cart for user: {}", user.getUsername());

        // Find user's cart
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + user.getUsername()));

        // Validate cart is not empty
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cannot create order from empty cart");
        }

        // Create new order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.Status.PENDING);

        // Convert cart items to order items
        Set<OrderItem> orderItems = cart.getCartItems().stream()
                .map(cartItem -> {
                    // Validate and fetch product
                    Product product = validateProductAvailability(cartItem.getProduct(), cartItem.getQuantity());

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProductId(product.getId());
                    orderItem.setProductName(product.getName());
                    orderItem.setUnitPrice(product.getPrice().longValue());
                    orderItem.setQuantity(cartItem.getQuantity());

                    // Reduce product quantity
                    product.setQuantity(product.getQuantity() - cartItem.getQuantity());
                    productRepository.save(product);

                    return orderItem;
                })
                .collect(Collectors.toSet());

        order.setOrderItems(orderItems);
        order.updateTotalAmount();

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Clear cart after order creation
        cart.getCartItems().clear();
        cartRepository.save(cart);

        log.info("Order created successfully for user: {}", user.getUsername());
        return convertToOrderDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUser(User user) {
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        return orders.stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
    }

    private OrderDTO convertToOrderDTO(Order order) {
        // Create OrderDTO
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);

        // Map order items with product details
        List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                .map(orderItem -> {
                    OrderItemDTO itemDTO = modelMapper.map(orderItem, OrderItemDTO.class);

                    // Fetch product details
                    Product product = productRepository.findById(orderItem.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

                    // Map product to ProductItemDTO
                    ProductItemDTO productItemDTO = modelMapper.map(product, ProductItemDTO.class);
                    itemDTO.setProduct(productItemDTO);
                    itemDTO.setSubtotal(orderItem.getUnitPrice() * orderItem.getQuantity());

                    return itemDTO;
                })
                .collect(Collectors.toList());

        orderDTO.setOrderItems(orderItemDTOs);

        return orderDTO;
    }

    private Product validateProductAvailability(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (product.getQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock for product: " + productId);
        }

        return product;
    }
}