package com.proyecto.spring_proyecto.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email String address,
        @NotBlank String password) {
}
