package com.proyecto.spring_proyecto.shared;

import java.util.Map;

public record ApiErrorResponse(
        String type,
        String title,
        int status,
        Map<String, String> fieldErrors) {
}
