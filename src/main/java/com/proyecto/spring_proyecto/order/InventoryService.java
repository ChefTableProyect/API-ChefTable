package com.proyecto.spring_proyecto.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.proyecto.spring_proyecto.core.entity.OrderItem;
import com.proyecto.spring_proyecto.order.dto.CreateOrderItemRequest;
import com.proyecto.spring_proyecto.repository.IProductoDAO;
import com.proyecto.spring_proyecto.shared.ApiException;

@Service
public class InventoryService {

    private final IProductoDAO productRepository;

    public InventoryService(IProductoDAO productRepository) {
        this.productRepository = productRepository;
    }

    public List<OrderLineReservation> reserve(List<CreateOrderItemRequest> requestedItems) {
        var productIds = requestedItems.stream().map(CreateOrderItemRequest::productId).toList();
        if (new HashSet<>(productIds).size() != productIds.size()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "DUPLICATE_PRODUCT",
                    "Cada plato debe aparecer una sola vez en el pedido");
        }

        var sortedIds = productIds.stream().sorted().toList();
        var lockedProducts = productRepository.findAllByIdForUpdate(sortedIds);
        if (lockedProducts.size() != sortedIds.size()) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "PRODUCT_NOT_FOUND",
                    "Uno de los platos ya no existe");
        }

        var productsById = new HashMap<Long, com.proyecto.spring_proyecto.core.entity.Producto>();
        lockedProducts.forEach(product -> productsById.put(product.getId(), product));

        return requestedItems.stream().map(requestedItem -> {
            var product = productsById.get(requestedItem.productId());
            if (!product.isActive()) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "PRODUCT_UNAVAILABLE",
                        product.getNombre() + " ya no está disponible");
            }
            if (product.getCantidad() < requestedItem.quantity()) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "INSUFFICIENT_STOCK",
                        "No hay unidades suficientes de " + product.getNombre());
            }

            product.setCantidad(product.getCantidad() - requestedItem.quantity());
            BigDecimal subtotal = product.getPrecio().multiply(BigDecimal.valueOf(requestedItem.quantity()));
            return new OrderLineReservation(
                    product,
                    requestedItem.quantity(),
                    normalizeNotes(requestedItem.notes()),
                    subtotal);
        }).toList();
    }

    public void release(List<OrderItem> orderItems) {
        var productIds = orderItems.stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .sorted()
                .toList();
        var lockedProducts = productRepository.findAllByIdForUpdate(productIds);
        var productsById = new HashMap<Long, com.proyecto.spring_proyecto.core.entity.Producto>();
        lockedProducts.forEach(product -> productsById.put(product.getId(), product));

        for (var item : orderItems) {
            var product = productsById.get(item.getProduct().getId());
            if (product == null) {
                throw new IllegalStateException("No se pudo restaurar el inventario del producto "
                        + item.getProduct().getId());
            }
            product.setCantidad(product.getCantidad() + item.getQuantity());
        }
    }

    private String normalizeNotes(String notes) {
        return notes == null || notes.isBlank() ? null : notes.trim();
    }
}
