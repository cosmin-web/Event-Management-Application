package com.example.eventservice.infrastructure.adapter.in.rest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.eventservice.application.dto.UserDTO;
import com.example.eventservice.application.mapper.UserMapper;
import com.example.eventservice.application.service.TokenBlacklistService;
import com.example.eventservice.application.service.UserService;
import com.example.eventservice.domain.model.UserEntity;
import com.example.eventservice.infrastructure.adapter.out.idm.IdmAuthClient;
import com.example.idm.grpc.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Autentificare si Inregistrare")
public class AuthController {

    private final IdmAuthClient idmAuthClient;
    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(IdmAuthClient idmAuthClient,
                          UserService userService,
                          TokenBlacklistService tokenBlacklistService) {
        this.idmAuthClient = idmAuthClient;
        this.userService = userService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Operation(summary = "Inregistrare utilizator nou")
    @ApiResponse(responseCode = "201", description = "Created. Cont creat cu succes.")
    @ApiResponse(responseCode = "400", description = "Bad Request. Rol invalid sau date formatate gresit.")
    @ApiResponse(responseCode = "401", description = "Unauthorized. Nu aveti permisiunea de a accesa resursa.")
    @ApiResponse(responseCode = "403", description = "Forbidden. Nu se pot crea conturi de ADMIN public.")
    @ApiResponse(responseCode = "409", description = "Conflict. Adresa de email exista deja.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Entity. Date lipsa (ex: parola).")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserDTO dto) {

        if (dto.getParola() == null || dto.getParola().trim().isEmpty()) {
            return ResponseEntity.unprocessableEntity()
                    .body(Map.of("message", "Parola este obligatorie la inregistrare."));
        }

        if ("ADMIN".equalsIgnoreCase(dto.getRol()) || "SERVICE_CLIENT".equalsIgnoreCase(dto.getRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Nu puteti crea conturi de Admin/Service public."));
        }

        if (!"CLIENT".equalsIgnoreCase(dto.getRol()) && !"OWNER_EVENT".equalsIgnoreCase(dto.getRol())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Rol invalid. Alegeti CLIENT sau OWNER_EVENT."));
        }

        try {
            UserEntity entity = UserMapper.toEntity(dto);
            userService.createUser(entity);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Cont creat cu succes!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Autentificare utilizator")
    @ApiResponse(responseCode = "201", description = "Created. Token generat cu succes.")
    @ApiResponse(responseCode = "400", description = "Bad Request. Cerere malformata.")
    @ApiResponse(responseCode = "401", description = "Unauthorized. Credentiale gresite.")
    @ApiResponse(responseCode = "403", description = "Forbidden. Cont blocat sau suspendat.")
    @ApiResponse(responseCode = "409", description = "Conflict. Utilizatorul este deja logat.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Entity. Email sau parola lipsesc.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.unprocessableEntity()
                    .body(Map.of("message", "Email si parola sunt obligatorii."));
        }

        LoginResponse response = idmAuthClient.login(email, password);

        if (response.getSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("token", response.getToken()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Login esuat. Credentiale gresite."));
        }
    }

    @Operation(summary = "Deconectare utilizator")
    @ApiResponse(responseCode = "200", description = "OK. Deconectare reusita.")
    @ApiResponse(responseCode = "400", description = "Bad Request. Header Authorization lipseste.")
    @ApiResponse(responseCode = "401", description = "Unauthorized. Token invalid.")
    @ApiResponse(responseCode = "403", description = "Forbidden.")
    @ApiResponse(responseCode = "409", description = "Conflict.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Entity. Token malformat.")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(name = "Authorization", required = false) String authHeader) {

        if (authHeader == null || authHeader.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Header Authorization lipseste."));
        }

        if (authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                DecodedJWT decoded = JWT.decode(token);
                Instant expiryDate = decoded.getExpiresAt().toInstant();

                tokenBlacklistService.blacklistToken(token, expiryDate);

                return ResponseEntity.ok(Map.of("message", "V-ati delogat cu succes!"));

            } catch (com.auth0.jwt.exceptions.JWTDecodeException e) {
                return ResponseEntity.unprocessableEntity()
                        .body(Map.of("message", "Format token invalid."));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Token invalid sau expirat."));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Format autorizare invalid."));
    }
}