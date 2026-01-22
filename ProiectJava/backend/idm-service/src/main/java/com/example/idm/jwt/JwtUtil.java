package com.example.idm.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

public class JwtUtil {

    private final Algorithm ALGORITHM;
    private static final String ISSUER = "idm-service";
    private static final long EXPIRATION_MINUTES = 60;

    private final JWTVerifier verifier;

    public JwtUtil(String secret) {
        this.ALGORITHM = Algorithm.HMAC256(secret);

        this.verifier = JWT.require(ALGORITHM)
                .withIssuer(ISSUER)
                .build();
    }

    public String generateToken(int userId, String role, String email) {
        Instant now = Instant.now();
        Instant exp = now.plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES);

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(String.valueOf(userId))
                .withExpiresAt(Date.from(exp))
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("role", role)
                .withClaim("email", email)
                .sign(ALGORITHM);
    }

    public DecodedJWT validate(String token) {
        return verifier.verify(token);
    }
}