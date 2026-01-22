package com.example.eventservice.application.auth;

import com.example.eventservice.domain.model.UserEntity;

public class AuthenticatedUser {

    private final Integer userId;
    private final UserEntity.Role role;
    private final String token;

    public AuthenticatedUser(Integer userId, UserEntity.Role role, String token) {
        this.userId = userId;
        this.role = role;
        this.token = token;
    }

    public Integer getUserId() { return userId; }
    public UserEntity.Role getRole() { return role; }
    public String getToken() { return token;}
}
