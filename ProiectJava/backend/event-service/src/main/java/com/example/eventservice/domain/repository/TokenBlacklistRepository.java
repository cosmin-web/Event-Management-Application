package com.example.eventservice.domain.repository;

import com.example.eventservice.domain.model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByToken(String token);
    void deleteByExpiryDateBefore(Instant now);
}