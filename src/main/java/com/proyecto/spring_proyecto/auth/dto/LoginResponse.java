package com.proyecto.spring_proyecto.auth.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private String address;
    private String token;
    private String role;

    public LoginResponse(String address, String token, String role) {
        this.address = address;
        this.token = token;
        this.role = role;
    }
    
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
}
