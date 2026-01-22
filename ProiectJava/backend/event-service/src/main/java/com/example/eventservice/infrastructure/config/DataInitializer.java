package com.example.eventservice.infrastructure.config;

import com.example.eventservice.domain.model.UserEntity;
import com.example.eventservice.domain.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;


    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new RuntimeException("Eroare la hashing parola!", ex);
        }
    }

    @PostConstruct
    public void init() {
        if (userRepository.count() == 0) {
            System.out.println("Baza de date este goala. Se adauga utilizatorii initiali...");

            userRepository.save(new UserEntity(
                    null,
                    "admin@local",
                    hashPassword("admin"),
                    UserEntity.Role.ADMIN
            ));

            userRepository.save(new UserEntity(
                    null,
                    "owner@local",
                    hashPassword("owner"),
                    UserEntity.Role.OWNER_EVENT
            ));

            userRepository.save(new UserEntity(
                    null,
                    "client@local",
                    hashPassword("client"),
                    UserEntity.Role.CLIENT
            ));

            userRepository.save(new UserEntity(
                    null,
                    "clients_service",
                    hashPassword("some_secret"),
                    UserEntity.Role.SERVICE_CLIENT
            ));

            System.out.println("Utilizatorii default au fost creati cu succes!");
        } else {
            System.out.println("Utilizatorii exista deja în baza de date. Nu se adauga altii.");
        }
    }
}
