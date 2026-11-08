package com.proyecto.spring_proyecto.application.interfaces;

import java.util.Optional;

import com.proyecto.spring_proyecto.auth.dto.LoginResponse;
import com.proyecto.spring_proyecto.auth.dto.RegisterUserRequest;
import com.proyecto.spring_proyecto.auth.dto.RegisterUserResponse;
import com.proyecto.spring_proyecto.core.entity.User;

public interface IUserService {
    Optional<LoginResponse> authenticate(String address, String rawPassword);
    LoginResponse renewToken(String token);
    RegisterUserResponse save(RegisterUserRequest user);
    
}
