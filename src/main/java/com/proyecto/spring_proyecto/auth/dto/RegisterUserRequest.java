package com.proyecto.spring_proyecto.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserRequest {

    private String address;    
    private String password;

}
