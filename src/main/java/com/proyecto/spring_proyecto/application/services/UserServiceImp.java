package com.proyecto.spring_proyecto.application.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.spring_proyecto.application.interfaces.IJwtService;
import com.proyecto.spring_proyecto.application.interfaces.IUserService;
import com.proyecto.spring_proyecto.auth.dto.LoginResponse;
import com.proyecto.spring_proyecto.auth.dto.RegisterUserRequest;
import com.proyecto.spring_proyecto.auth.dto.RegisterUserResponse;
import com.proyecto.spring_proyecto.core.entity.User;
import com.proyecto.spring_proyecto.repository.IUserDAO;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserServiceImp implements IUserService {
    private final IUserDAO userDAO;
    private final IJwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImp(IUserDAO userDAO, IJwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<LoginResponse> authenticate(String address, String rawPassword) {
        return userDAO.findByAddress(address)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                .map(u -> new LoginResponse(
                        u.getAddress(),
                        jwtService.generateToken(u.getAddress(), u.getRole().name()),
                        u.getRole().name()));
    }

    @Override
    public LoginResponse renewToken(String token) {
        var claims = jwtService.parseClaims(token);
        var address = claims.getSubject();
        return userDAO.findByAddress(address)
                .map(u -> new LoginResponse(
                        address,
                        jwtService.generateToken(address, u.getRole().name()),
                        u.getRole().name()))
                .orElse(null);
    }

    @Override
    public RegisterUserResponse save(RegisterUserRequest request) {

        RegisterUserResponse response = new RegisterUserResponse();

        // 1️⃣ Validación de negocio
        if (userDAO.findByAddress(request.getAddress()).isPresent()) {
            response.setSuccess(false);
            response.setMessage("El correo ya está registrado");
            return response;
        }

        // 2️⃣ Mapear request → entidad
        User user = new User();
        user.setAddress(request.getAddress());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 3️⃣ Persistir
        user = userDAO.save(user);

        // 4️⃣ Respuesta
        response.setSuccess(true);
        response.setMessage("Usuario registrado correctamente");
        response.setUserId(user.getId());

        return response;
    }
}
