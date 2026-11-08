package com.proyecto.spring_proyecto.order;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.spring_proyecto.core.entity.OrderStatus;
import com.proyecto.spring_proyecto.order.dto.CreateOrderRequest;
import com.proyecto.spring_proyecto.order.dto.OrderResponse;
import com.proyecto.spring_proyecto.repository.ICustomerOrderDAO;
import com.proyecto.spring_proyecto.shared.ApiException;

@Service
public class OrderApplicationService {

    private final ICustomerOrderDAO orderRepository;
    private final OrderCreationService orderCreationService;
    private final OrderIdempotencyService idempotencyService;
    private final InventoryService inventoryService;
    private final OrderResponseMapper responseMapper;

    public OrderApplicationService(
            ICustomerOrderDAO orderRepository,
            OrderCreationService orderCreationService,
            OrderIdempotencyService idempotencyService,
            InventoryService inventoryService,
            OrderResponseMapper responseMapper) {
        this.orderRepository = orderRepository;
        this.orderCreationService = orderCreationService;
        this.idempotencyService = idempotencyService;
        this.inventoryService = inventoryService;
        this.responseMapper = responseMapper;
    }

    public OrderCreationResult create(String rawIdempotencyKey, CreateOrderRequest request) {
        var idempotencyKey = idempotencyService.normalizeKey(rawIdempotencyKey);
        var fingerprint = idempotencyService.fingerprint(request);
        var existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
        if (existingOrder.isPresent()) {
            idempotencyService.verifySameRequest(existingOrder.get(), fingerprint);
            return new OrderCreationResult(responseMapper.toResponse(existingOrder.get()), true);
        }

        try {
            var createdOrder = orderCreationService.createNew(idempotencyKey, fingerprint, request);
            return new OrderCreationResult(responseMapper.toResponse(createdOrder), false);
        } catch (DataIntegrityViolationException exception) {
            // Dos solicitudes con la misma clave pueden llegar al mismo tiempo. La restricción
            // única decide cuál crea el pedido y esta consulta recupera el resultado ganador.
            var concurrentOrder = orderRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> exception);
            idempotencyService.verifySameRequest(concurrentOrder, fingerprint);
            return new OrderCreationResult(responseMapper.toResponse(concurrentOrder), true);
        }
    }

    @Transactional(readOnly = true)
    public OrderResponse findPublic(String publicCode) {
        return orderRepository.findByPublicCodeIgnoreCase(publicCode)
                .map(responseMapper::toResponse)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "No encontramos ese pedido"));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findKitchenQueue() {
        return orderRepository.findAllByStatusInOrderByCreatedAtAsc(
                        EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY))
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus nextStatus) {
        var order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "No encontramos ese pedido"));

        if (!isValidTransition(order.getStatus(), nextStatus)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "INVALID_ORDER_TRANSITION",
                    "El pedido no puede pasar de " + order.getStatus() + " a " + nextStatus);
        }

        if (nextStatus == OrderStatus.CANCELLED && order.getStatus() == OrderStatus.CONFIRMED) {
            inventoryService.release(order.getItems());
        }
        order.setStatus(nextStatus);
        order.setUpdatedAt(Instant.now());
        return responseMapper.toResponse(order);
    }

    private boolean isValidTransition(OrderStatus current, OrderStatus next) {
        if (next == OrderStatus.CANCELLED) {
            return current == OrderStatus.CONFIRMED || current == OrderStatus.PREPARING;
        }
        return switch (current) {
            case CONFIRMED -> next == OrderStatus.PREPARING;
            case PREPARING -> next == OrderStatus.READY;
            case READY -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

}
