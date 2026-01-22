package com.example.eventservice.application.mapper;

import com.example.eventservice.application.dto.UserDTO;
import com.example.eventservice.domain.model.UserEntity;

public class UserMapper {

    public static UserDTO fromEntity(UserEntity entity) {
        if (entity == null) return null;

        return new UserDTO(
                entity.getId(),
                entity.getEmail(),
                entity.getRol().name()
        );
    }

    public static UserEntity toEntity(UserDTO dto) {
        if (dto == null) return null;

        UserEntity entity = new UserEntity();
        entity.setId(dto.getId());
        entity.setEmail(dto.getEmail());

        entity.setParola(dto.getParola());

        if (dto.getRol() != null) {
            entity.setRol(UserEntity.Role.fromString(dto.getRol()));
        } else {
            entity.setRol(UserEntity.Role.CLIENT);
        }

        return entity;
    }
}