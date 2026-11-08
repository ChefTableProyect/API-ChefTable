package com.proyecto.spring_proyecto.menu.dto;

import java.math.BigDecimal;

public record MenuItemResponse(
        Long id,
        String name,
        BigDecimal price,
        int stock,
        boolean active,
        boolean available,
        Long categoryId,
        String categoryName) {
}
