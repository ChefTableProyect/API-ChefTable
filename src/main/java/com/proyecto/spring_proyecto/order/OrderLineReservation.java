package com.proyecto.spring_proyecto.order;

import java.math.BigDecimal;

import com.proyecto.spring_proyecto.core.entity.Producto;

public record OrderLineReservation(
        Producto product,
        int quantity,
        String notes,
        BigDecimal subtotal) {
}
