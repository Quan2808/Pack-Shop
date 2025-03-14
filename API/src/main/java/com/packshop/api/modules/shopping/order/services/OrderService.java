package com.packshop.api.modules.shopping.order.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.packshop.api.common.exceptions.InsufficientStockException;
import com.packshop.api.common.exceptions.InvalidStatusException;
import com.packshop.api.common.exceptions.ResourceNotFoundException;
import com.packshop.api.modules.catalog.entities.product.Product;
import com.packshop.api.modules.catalog.repositories.ProductRepository;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.address.entity.Address;
import com.packshop.api.modules.shopping.address.repository.AddressRepository;
import com.packshop.api.modules.shopping.cart.entities.Cart;
import com.packshop.api.modules.shopping.cart.repositories.CartRepository;
import com.packshop.api.modules.shopping.cart.service.CartService;
import com.packshop.api.modules.shopping.dto.ProductItemDTO;
import com.packshop.api.modules.shopping.order.dto.OrderDTO;
import com.packshop.api.modules.shopping.order.dto.OrderItemDTO;
import com.packshop.api.modules.shopping.order.entities.Order;
import com.packshop.api.modules.shopping.order.entities.OrderItem;
import com.packshop.api.modules.shopping.order.repositories.OrderRepository;
import com.packshop.api.modules.shopping.payment.PaymentTransaction;
import com.packshop.api.modules.shopping.payment.PaymentTransactionDTO;
import com.packshop.api.modules.shopping.payment.repository.PaymentTransactionRepository;
import com.packshop.api.modules.shopping.payment.utility.MoMoSignatureGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RestTemplate restTemplate;
    private final MoMoSignatureGenerator signatureGenerator;

    private static final String MOMO_PARTNER_CODE = "MOMOBKUN20180529";
    private static final String MOMO_ACCESS_KEY = "klm05TvNBzhg7h7j";
    private static final String MOMO_SECRET_KEY = "at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa";
    private static final String MOMO_API_ENDPOINT = "https://test-payment.momo.vn/v2/gateway/api/create";

    @Transactional
    public OrderDTO createOrderFromCartWithMoMo(User user, Long addressId) {
        log.info("Creating order from cart with MoMo payment (Sandbox) for user: {}", user.getUsername());

        // Tìm địa chỉ của người dùng
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found for user: " + user.getUsername()));

        // Tìm giỏ hàng
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + user.getUsername()));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cannot create order from empty cart");
        }

        // Tạo đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.Status.PENDING);

        Set<OrderItem> orderItems = cart.getCartItems().stream()
                .map(cartItem -> {
                    Product product = validateProductAvailability(cartItem.getProduct(), cartItem.getQuantity());
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProductId(product.getId());
                    orderItem.setProductName(product.getName());
                    orderItem.setUnitPrice(product.getPrice().longValue());
                    orderItem.setQuantity(cartItem.getQuantity());
                    product.setQuantity(product.getQuantity() - cartItem.getQuantity());
                    productRepository.save(product);
                    return orderItem;
                })
                .collect(Collectors.toSet());

        order.setOrderItems(orderItems);
        order.updateTotalAmount();
        order.setAddress(address.getFullAddress());

        // Lưu order trước để có ID hợp lệ
        Order savedOrder = orderRepository.save(order);

        // Tạo giao dịch MoMo trong Sandbox với savedOrder
        PaymentTransaction paymentTransaction = createMoMoPaymentTransaction(savedOrder);
        savedOrder.setPaymentTransaction(paymentTransaction);

        // Lưu lại order sau khi thêm paymentTransaction
        orderRepository.save(savedOrder);

        // Xóa giỏ hàng
        cart.getCartItems().clear();
        cartService.clearCart(user);
        cartRepository.save(cart);

        log.info("Order created successfully with MoMo payment (Sandbox) for user: {}", user.getUsername());
        return convertToOrderDTO(savedOrder);
    }

    private PaymentTransaction createMoMoPaymentTransaction(Order order) {
        String requestId = "REQ_" + System.currentTimeMillis();
        String orderId = "ORDER_" + order.getId() + "_" + System.currentTimeMillis();
        String signature = signatureGenerator.generateSignature(requestId, orderId, order.getTotalAmount());

        // Tạo payload cho API MoMo Sandbox
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", MOMO_PARTNER_CODE);
        requestBody.put("requestId", requestId);
        requestBody.put("orderId", orderId);
        requestBody.put("amount", order.getTotalAmount());
        requestBody.put("orderInfo", "Payment for order #" + order.getId() + " (Sandbox Test)");
        requestBody.put("redirectUrl", "http://localhost:8080/payment/return");
        requestBody.put("ipnUrl", "http://localhost:8080/payment/ipn");
        requestBody.put("requestType", "captureWallet");
        requestBody.put("extraData", "");
        requestBody.put("signature", signature);
        requestBody.put("lang", "vi");

        log.info("MoMo Request Payload: {}", requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    MOMO_API_ENDPOINT,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null || !response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to create MoMo payment transaction: " + response.getStatusCode());
            }

            log.info("MoMo Response: {}", responseBody);

            // Tạo PaymentTransaction từ phản hồi của MoMo
            PaymentTransaction paymentTransaction = new PaymentTransaction();
            paymentTransaction.setOrder(order);
            // Dùng orderId thay vì transId vì MoMo không trả về transId trong sandbox
            paymentTransaction.setTransactionId((String) responseBody.get("orderId")); // Hoặc
                                                                                       // responseBody.get("requestId")
            paymentTransaction.setRequestId(requestId);
            paymentTransaction.setPartnerCode(MOMO_PARTNER_CODE);
            paymentTransaction.setAmount(order.getTotalAmount());
            paymentTransaction.setStatus(PaymentTransaction.PaymentStatus.PENDING);
            paymentTransaction.setPayUrl((String) responseBody.get("payUrl"));
            paymentTransaction.setQrCodeUrl((String) responseBody.get("qrCodeUrl"));
            paymentTransaction.setTransactionDate(LocalDateTime.now());
            paymentTransaction.setResponseMessage((String) responseBody.get("message"));

            return paymentTransactionRepository.save(paymentTransaction);
        } catch (Exception e) {
            log.error("Error calling MoMo Sandbox API: {}", e.getMessage());
            throw new RuntimeException("Failed to integrate with MoMo Sandbox", e);
        }
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUser(User user) {
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        return orders.stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId, User user) {
        log.info("Retrieving order {} for user {}", orderId, user.getUsername());

        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        return convertToOrderDTO(order);
    }

    @Transactional
    public OrderDTO createOrderFromCart(User user, Long addressId) {
        log.info("Creating order from cart for user: {}", user.getUsername());

        // Find user's address
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found for user: " + user.getUsername()));

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
        order.setAddress(address.getFullAddress());

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Clear cart after order creation
        cart.getCartItems().clear();
        cartService.clearCart(user);
        cartRepository.save(cart);

        log.info("Order created successfully for user: {}", user.getUsername());
        return convertToOrderDTO(savedOrder);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, Order.Status newStatus, User user) {
        log.info("Updating order {} status to {}", orderId, newStatus);

        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        validateStatusTransition(order, newStatus);

        // Status transition validation can be added here
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        return convertToOrderDTO(updatedOrder);
    }

    @Transactional
    public void cancelOrder(Long orderId, User user) {
        log.info("Cancelling order {} for user {}", orderId, user.getUsername());

        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Only allow cancellation of pending orders
        validateStatusTransition(order, order.getStatus());

        // Restore product quantities
        order.getOrderItems().forEach(orderItem -> {
            Product product = productRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            product.setQuantity(product.getQuantity() + orderItem.getQuantity());
            productRepository.save(product);
        });

        order.setStatus(Order.Status.CANCELLED);
        orderRepository.save(order);

        log.info("Order {} cancelled successfully", orderId);
    }

    private OrderDTO convertToOrderDTO(Order order) {
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);

        List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                .map(orderItem -> {
                    OrderItemDTO itemDTO = modelMapper.map(orderItem, OrderItemDTO.class);
                    Product product = productRepository.findById(orderItem.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                    ProductItemDTO productItemDTO = modelMapper.map(product, ProductItemDTO.class);
                    itemDTO.setProduct(productItemDTO);
                    itemDTO.setSubtotal(orderItem.getUnitPrice() * orderItem.getQuantity());
                    return itemDTO;
                })
                .collect(Collectors.toList());

        orderDTO.setOrderItems(orderItemDTOs);

        if (order.getPaymentTransaction() != null) {
            orderDTO.setPaymentTransaction(modelMapper.map(order.getPaymentTransaction(), PaymentTransactionDTO.class));
        }

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

    private void validateStatusTransition(Order order, Order.Status newStatus) {
        Order.Status currentStatus = order.getStatus();
        String orderId = order.getId().toString();

        if (currentStatus == Order.Status.CANCELLED) {
            throw new InvalidStatusException(
                    orderId,
                    currentStatus.name(),
                    "any non-CANCELLED status",
                    Order.Status.class);
        }

        switch (currentStatus) {
            case PENDING:
                if (newStatus != Order.Status.SHIPPED && newStatus != Order.Status.CANCELLED) {
                    throw new InvalidStatusException(
                            orderId,
                            currentStatus.name(),
                            "SHIPPED or CANCELLED",
                            Order.Status.class);
                }
                break;
            case SHIPPED:
                if (newStatus != Order.Status.DELIVERED) {
                    throw new InvalidStatusException(
                            orderId,
                            currentStatus.name(),
                            "DELIVERED",
                            Order.Status.class);
                }
                break;
            case DELIVERED:
                throw new InvalidStatusException(
                        orderId,
                        currentStatus.name(),
                        "no further status change allowed",
                        Order.Status.class);
            default:
                throw new IllegalStateException("Unexpected status: " + currentStatus);
        }
    }
}