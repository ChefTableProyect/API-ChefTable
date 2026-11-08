package com.proyecto.spring_proyecto.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserResponse {
    private boolean success;
    private String message;
    private Long userId;
}
