package com.proyecto.spring_proyecto.order.dto;

import com.proyecto.spring_proyecto.core.entity.OrderStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
}
