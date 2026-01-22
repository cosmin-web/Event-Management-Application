package com.example.clientservice.application.auth;

import com.example.clientservice.domain.model.UserRole;

public class AuthenticatedUser {

    private final Integer userId;
    private final UserRole role;
    private final String token;
    private final String email;

    public AuthenticatedUser(Integer userId, UserRole role, String token, String email) {
        this.userId = userId;
        this.role = role;
        this.token = token;
        this.email = email;
    }

    public Integer getUserId() { return userId; }
    public UserRole getRole() { return role; }
    public String getToken() { return token; }
    public String getEmail() { return email; }
}
