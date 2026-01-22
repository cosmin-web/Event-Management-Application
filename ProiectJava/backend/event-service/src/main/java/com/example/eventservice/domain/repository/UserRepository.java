package com.example.eventservice.domain.repository;

import com.example.eventservice.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface UserRepository extends JpaRepository<UserEntity, Integer>
{
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByRol(UserEntity.Role rol);
    boolean existsByEmail(String email);
}