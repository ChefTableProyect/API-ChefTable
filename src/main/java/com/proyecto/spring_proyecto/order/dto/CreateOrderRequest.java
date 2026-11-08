package com.proyecto.spring_proyecto.order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotBlank @Size(max = 40) String tableCode,
        @NotEmpty @Size(max = 50) List<@Valid CreateOrderItemRequest> items) {
}
