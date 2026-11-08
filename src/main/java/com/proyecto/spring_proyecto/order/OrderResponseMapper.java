package com.proyecto.spring_proyecto.order;

import org.springframework.stereotype.Component;

import com.proyecto.spring_proyecto.core.entity.CustomerOrder;
import com.proyecto.spring_proyecto.order.dto.OrderItemResponse;
import com.proyecto.spring_proyecto.order.dto.OrderResponse;

@Component
public class OrderResponseMapper {

    public OrderResponse toResponse(CustomerOrder order) {
        var items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getItemName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getNotes(),
                        item.getSubtotal()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getPublicCode(),
                order.getDiningTable().getName(),
                order.getStatus(),
                order.getTotal(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items);
    }
}
