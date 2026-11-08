package com.proyecto.spring_proyecto.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.proyecto.spring_proyecto.application.interfaces.IUserService;
import com.proyecto.spring_proyecto.auth.dto.LoginRequest;
import com.proyecto.spring_proyecto.auth.dto.LoginResponse;
import com.proyecto.spring_proyecto.auth.dto.RegisterUserRequest;
import com.proyecto.spring_proyecto.auth.dto.RegisterUserResponse;
import com.proyecto.spring_proyecto.core.entity.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permitir acceso desde el frontend

public class LoginController {

    private final IUserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return userService.authenticate(request.address(), request.password())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(@RequestBody RegisterUserRequest request) {
        RegisterUserResponse userReponse = userService.save(request);
        return ResponseEntity.ok(userReponse);
    }

}
