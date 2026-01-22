package com.example.eventservice.domain.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "token_blacklist")
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512, unique = true)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    public RevokedToken() {}

    public RevokedToken(String token, Instant expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }


    public String getToken() { return token; }
    public Instant getExpiryDate() { return expiryDate; }
}