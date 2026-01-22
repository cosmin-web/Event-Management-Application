package com.example.clientservice.application.auth;

import com.example.clientservice.domain.model.UserRole;
import com.example.clientservice.infrastructure.adapter.out.idm.IdmAuthClient;
import com.example.idm.grpc.ValidateTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Service
public class AuthorizationService {

    private final IdmAuthClient idmAuthClient;

    public AuthorizationService(IdmAuthClient idmAuthClient) {
        this.idmAuthClient = idmAuthClient;
    }

    public AuthenticatedUser requireUser(String authorizationHeader, UserRole... allowedRoles) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Missing or invalid Authorization header");
        }

        String token = authorizationHeader.substring("Bearer ".length());

        if (token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Empty token");
        }

        ValidateTokenResponse resp = idmAuthClient.validate(token);

        if (!resp.getValid()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, resp.getMessage());
        }

        UserRole role = UserRole.fromString(resp.getRole());

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

        String email = resp.getEmail();

        return new AuthenticatedUser(userId, role, token, email);
    }
}
