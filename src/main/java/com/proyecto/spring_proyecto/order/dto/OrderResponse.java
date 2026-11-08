package com.proyecto.spring_proyecto.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.proyecto.spring_proyecto.core.entity.OrderStatus;

public record OrderResponse(
        Long id,
        String publicCode,
        String tableName,
        OrderStatus status,
        BigDecimal total,
        Instant createdAt,
        Instant updatedAt,
        List<OrderItemResponse> items) {
}
