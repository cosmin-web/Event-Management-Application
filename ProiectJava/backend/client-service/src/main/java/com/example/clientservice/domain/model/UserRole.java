package com.example.clientservice.domain.model;

public enum UserRole {
    ADMIN,
    OWNER_EVENT,
    CLIENT,
    SERVICE_CLIENT;

    public static UserRole fromString(String value) {
        if (value == null) return null;
        return UserRole.valueOf(value.trim().toUpperCase());
    }
}
