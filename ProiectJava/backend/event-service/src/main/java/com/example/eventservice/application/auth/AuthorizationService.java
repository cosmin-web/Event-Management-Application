package com.example.eventservice.application.auth;

import com.example.eventservice.application.service.TokenBlacklistService;
import com.example.eventservice.domain.model.UserEntity;
import com.example.eventservice.infrastructure.adapter.out.idm.IdmAuthClient;
import com.example.idm.grpc.ValidateTokenResponse;
import com.google.api.Http;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Service
public class AuthorizationService {

    private final IdmAuthClient idmAuthClient;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthorizationService(IdmAuthClient idmAuthClient, TokenBlacklistService tokenBlacklistService) {
        this.idmAuthClient = idmAuthClient;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public AuthenticatedUser requireUser(String authorizationHeader, UserEntity.Role... allowedRoles) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Missing or invalid Authorization header"
            );
        }

        String token = authorizationHeader.substring("Bearer ".length());

        if (token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Empty token");
        }

        if (tokenBlacklistService.isBlacklisted(token)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Sesiune expirata sau revocata. Va rugam sa va logati din nou"
            );
        }

        ValidateTokenResponse resp = idmAuthClient.validate(token);

        if (!resp.getValid()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, resp.getMessage());
        }

        UserEntity.Role role = UserEntity.Role.fromString(resp.getRole());

        if (allowedRoles != null && allowedRoles.length > 0) {
            boolean ok = Arrays.stream(allowedRoles).anyMatch(r -> r == role);
            if (!ok) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Insufficient permissions"
                );
            }
        }

        Integer userId;
        try {
            userId = Integer.valueOf(resp.getSub());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid user id in token"
            );
        }

        return new AuthenticatedUser(userId, role, token);
    }
}
