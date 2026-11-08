package com.proyecto.spring_proyecto.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.spring_proyecto.order.dto.CreateOrderRequest;
import com.proyecto.spring_proyecto.order.dto.OrderResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
public class PublicOrderController {

    private final OrderApplicationService orderService;

    public PublicOrderController(OrderApplicationService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request) {
        var result = orderService.create(idempotencyKey, request);
        return ResponseEntity
                .status(result.replayed() ? HttpStatus.OK : HttpStatus.CREATED)
                .header("Idempotency-Replayed", Boolean.toString(result.replayed()))
                .body(result.order());
    }

    @GetMapping("/{publicCode}")
    public OrderResponse find(@PathVariable String publicCode) {
        return orderService.findPublic(publicCode);
    }
}
