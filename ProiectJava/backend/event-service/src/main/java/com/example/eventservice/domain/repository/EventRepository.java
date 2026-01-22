package com.example.eventservice.domain.repository;

import com.example.eventservice.domain.model.EventEntity;
import com.example.eventservice.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Integer>
{
    List<EventEntity> findByOwner(UserEntity owner);
    List<EventEntity> findByNumeContainingIgnoreCase(String nume);
    List<EventEntity> findByLocatieContainingIgnoreCase(String locatie);
    List<EventEntity> findByDescriereContainingIgnoreCase(String descriere);
}