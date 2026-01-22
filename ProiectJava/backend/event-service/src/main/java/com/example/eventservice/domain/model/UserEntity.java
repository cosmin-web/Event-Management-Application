package com.example.eventservice.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "utilizatori")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String parola;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role rol;

    public enum Role {
        ADMIN, OWNER_EVENT, CLIENT, SERVICE_CLIENT;

        @com.fasterxml.jackson.annotation.JsonCreator
        public static Role fromString(String value) {
            if (value == null) return null;
            return Role.valueOf(value.trim().toUpperCase().replace("-", "_"));
        }
    }

    public UserEntity() {}

    public UserEntity(Integer id, String email, String parola, Role rol) {
        this.id = id;
        this.email = email;
        this.parola = parola;
        this.rol = rol;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getParola() { return parola; }
    public void setParola(String parola) { this.parola = parola; }

    public Role getRol() { return rol; }
    public void setRol(Role rol) { this.rol = rol; }
}