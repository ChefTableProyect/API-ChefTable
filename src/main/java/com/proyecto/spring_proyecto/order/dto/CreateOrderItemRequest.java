package com.proyecto.spring_proyecto.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderItemRequest(
        @NotNull Long productId,
        @Min(1) int quantity,
        @Size(max = 300) String notes) {
}
