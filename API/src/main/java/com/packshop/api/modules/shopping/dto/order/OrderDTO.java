package com.packshop.api.modules.shopping.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class OrderDTO {
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'HH:mm")
    private LocalDateTime orderDate;
    private Status status;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> orderItems;

    public enum Status {
        PENDING,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }
}