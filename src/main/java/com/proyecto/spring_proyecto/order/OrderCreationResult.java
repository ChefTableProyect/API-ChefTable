package com.proyecto.spring_proyecto.order;

import com.proyecto.spring_proyecto.order.dto.OrderResponse;

public record OrderCreationResult(OrderResponse order, boolean replayed) {
}
