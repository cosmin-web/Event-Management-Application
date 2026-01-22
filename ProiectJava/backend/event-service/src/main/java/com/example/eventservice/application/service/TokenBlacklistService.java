package com.example.eventservice.application.service;

import com.example.eventservice.domain.model.RevokedToken;
import com.example.eventservice.domain.repository.TokenBlacklistRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class TokenBlacklistService {
    private final TokenBlacklistRepository repository;

    public TokenBlacklistService(TokenBlacklistRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void blacklistToken(String token, Instant expiryDate) {
        if (!repository.existsByToken(token)) {
            repository.save(new RevokedToken(token, expiryDate));
        }
    }

    public boolean isBlacklisted(String token) {
        return repository.existsByToken(token);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanExpiredTokens() {
        repository.deleteByExpiryDateBefore(Instant.now());
    }
}