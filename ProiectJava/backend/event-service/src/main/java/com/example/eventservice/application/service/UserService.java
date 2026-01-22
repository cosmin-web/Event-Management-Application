package com.example.eventservice.application.service;

import com.example.eventservice.domain.model.UserEntity;
import com.example.eventservice.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Optional;


@Service
public class UserService {

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

    public List<UserEntity> getAllUsers2() {
        return userRepository.findAll();
    }

    public Optional<UserEntity> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<UserEntity> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserEntity createUser(UserEntity user) {
        if(userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Emailul este deja utilizat.");
        }
        user.setParola(hashPassword(user.getParola()));
        return userRepository.save(user);
    }

    public UserEntity updateUser(Integer id, UserEntity updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEmail(updatedUser.getEmail());

                    if (updatedUser.getParola() != null && !updatedUser.getParola().trim().isEmpty()) {
                        user.setParola(hashPassword(updatedUser.getParola()));
                    }
                    user.setRol(updatedUser.getRol());

                    return userRepository.save(user);
                }).orElseThrow(() -> new IllegalArgumentException("Utilizatorul nu exista."));
    }

    public void deleteUser(Integer id) {
        if(!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Utilizatorul nu exista.");
        }
        userRepository.deleteById(id);
    }
}
