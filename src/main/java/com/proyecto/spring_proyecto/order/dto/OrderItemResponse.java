package com.proyecto.spring_proyecto.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String name,
        BigDecimal unitPrice,
        int quantity,
        String notes,
        BigDecimal subtotal) {
}
