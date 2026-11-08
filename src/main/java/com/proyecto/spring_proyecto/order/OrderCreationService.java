package com.proyecto.spring_proyecto.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.spring_proyecto.core.entity.CustomerOrder;
import com.proyecto.spring_proyecto.core.entity.OrderItem;
import com.proyecto.spring_proyecto.core.entity.OrderStatus;
import com.proyecto.spring_proyecto.order.dto.CreateOrderRequest;
import com.proyecto.spring_proyecto.repository.ICustomerOrderDAO;
import com.proyecto.spring_proyecto.repository.IDiningTableDAO;
import com.proyecto.spring_proyecto.shared.ApiException;

@Service
public class OrderCreationService {

    private final ICustomerOrderDAO orderRepository;
    private final IDiningTableDAO tableRepository;
    private final InventoryService inventoryService;

    public OrderCreationService(
            ICustomerOrderDAO orderRepository,
            IDiningTableDAO tableRepository,
            InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.tableRepository = tableRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional(timeout = 10)
    public CustomerOrder createNew(String idempotencyKey, String fingerprint, CreateOrderRequest request) {
        var table = tableRepository.findByCodeIgnoreCaseAndActiveTrue(request.tableCode().trim())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "TABLE_NOT_FOUND", "La mesa no existe o no está activa"));

        var order = new CustomerOrder();
        order.setPublicCode(createPublicCode());
        order.setIdempotencyKey(idempotencyKey);
        order.setRequestFingerprint(fingerprint);
        order.setDiningTable(table);
        order.setStatus(OrderStatus.CONFIRMED);

        var now = Instant.now();
        order.setTotal(BigDecimal.ZERO);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        // Se fuerza primero la restricción única de idempotencia. Un reintento
        // concurrente espera aquí y nunca llega a descontar inventario dos veces.
        orderRepository.saveAndFlush(order);

        var reservations = inventoryService.reserve(request.items());

        BigDecimal total = BigDecimal.ZERO;
        for (var reservation : reservations) {
            var item = new OrderItem();
            item.setProduct(reservation.product());
            item.setItemName(reservation.product().getNombre());
            item.setUnitPrice(reservation.product().getPrecio());
            item.setQuantity(reservation.quantity());
            item.setNotes(reservation.notes());
            item.setSubtotal(reservation.subtotal());
            order.addItem(item);
            total = total.add(reservation.subtotal());
        }

        order.setTotal(total);
        return order;
    }

    private String createPublicCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
    }
}
