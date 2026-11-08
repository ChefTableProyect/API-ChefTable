package com.proyecto.spring_proyecto.order;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.spring_proyecto.order.dto.OrderResponse;
import com.proyecto.spring_proyecto.order.dto.UpdateOrderStatusRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/kitchen/orders")
public class KitchenOrderController {

    private final OrderApplicationService orderService;

    public KitchenOrderController(OrderApplicationService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> findQueue() {
        return orderService.findKitchenQueue();
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }
}
