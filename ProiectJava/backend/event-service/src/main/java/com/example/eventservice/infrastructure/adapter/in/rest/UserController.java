package com.example.eventservice.infrastructure.adapter.in.rest;

import com.example.eventservice.application.auth.AuthenticatedUser;
import com.example.eventservice.application.dto.UserDTO;
import com.example.eventservice.application.mapper.UserMapper;
import com.example.eventservice.application.auth.AuthorizationService;
import com.example.eventservice.domain.model.UserEntity;
import com.example.eventservice.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/event-manager/users")
@Tag(name = "Users", description = "Operatii pentru gestionarea utilizatorilor sistemului")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthorizationService authorizationService;

    private EntityModel<UserDTO> toModel(UserDTO dto) {
        EntityModel<UserDTO> model = EntityModel.of(dto);

        model.add(linkTo(methodOn(UserController.class).getUserById(dto.getId(), null)).withSelfRel());
        model.add(linkTo(UserController.class).withRel("parent"));

        return model;
    }

    @Operation(summary = "Listare utilizatori")
    @ApiResponse(responseCode = "200", description = "Ok. Lista utilizatorilor a fost returnata.")
    @ApiResponse(responseCode = "404", description = "Not Found. Resursa nu a fost gasita.")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UserDTO>>> getAllUsers(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    ) {
        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.ADMIN
        );

        List<EntityModel<UserDTO>> list = userService.getAllUsers2().stream()
                .map(UserMapper::fromEntity)
                .map(this::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(list));
    }

    @Operation(summary = "Obtine un utilizator dupa ID")
    @ApiResponse(responseCode = "200", description = "Ok. Utilizatorul a fost gasit.")
    @ApiResponse(responseCode = "404", description = "Not Found. Utilizatorul nu a fost gasit.")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserDTO>> getUserById(
            @PathVariable Integer id,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    ) {
        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.ADMIN
        );

        return userService.getUserById(id)
                .map(UserMapper::fromEntity)
                .map(this::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private void createClientProfile(UserDTO userDto, String token) {
        try {
            String url = "http://localhost:8082/api/client-service/clients";
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> body = new HashMap<>();
            body.put("email", userDto.getEmail());
            body.put("isPublic", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", token);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(url, request, Object.class);
        } catch (Exception e) {
            System.err.println("Nu s-a putut crea profilul Mongo: " + e.getMessage());
        }
    }

    private void syncEmailWithMongo(String oldEmail, String newEmail, String token) {
        try {
            String url = "http://localhost:8082/api/client-service/clients/sync-email";
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> body = new HashMap<>();
            body.put("oldEmail", oldEmail);
            body.put("newEmail", newEmail);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", token);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            restTemplate.put(url, request);
        } catch (Exception e) {
            System.err.println("Eroare sincronizare Mongo: " + e.getMessage());
        }
    }

    @Operation(summary = "Creeaza un utilizator nou")
    @ApiResponse(responseCode = "201", description = "Created. Utilizatorul a fost creat.")
    @ApiResponse(responseCode = "409", description = "Conflict. Adresa de email exista deja.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Content. Date invalide (ex: parola lipsa).")
    @PostMapping
    public ResponseEntity<EntityModel<UserDTO>> createUser(
            @RequestBody @Valid UserDTO dto,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.ADMIN
        );

        if (dto.getParola() == null || dto.getParola().trim().isEmpty()) {
            return ResponseEntity.unprocessableEntity().build();
        }

        UserEntity entity = UserMapper.toEntity(dto);
        UserEntity saved = userService.createUser(entity);
        UserDTO response = UserMapper.fromEntity(saved);

        createClientProfile(dto, authorizationHeader);

        EntityModel<UserDTO> model = toModel(response);
        URI location = model.getRequiredLink("self").toUri();
        return ResponseEntity.created(location).body(model);
    }

    @Operation(summary = "Actualizeaza un utilizator")
    @ApiResponse(responseCode = "201", description = "Created.")
    @ApiResponse(responseCode = "204", description = "No Content. Utilizator actualizat.")
    @ApiResponse(responseCode = "409", description = "Conflict.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Content.")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserDTO>> updateUser(
            @PathVariable Integer id,
            @RequestBody @Valid UserDTO dto,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.ADMIN
        );

        UserEntity existingEntity = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String oldEmail = existingEntity.getEmail();
        String newEmail = dto.getEmail();

        UserEntity updatedEntity = UserMapper.toEntity(dto);
        UserEntity saved = userService.updateUser(id, updatedEntity);

        if (!oldEmail.equalsIgnoreCase(newEmail)) {
            syncEmailWithMongo(oldEmail, newEmail, authorizationHeader);
        }

        UserDTO response = UserMapper.fromEntity(saved);
        return ResponseEntity.ok(toModel(response));
    }

    @Operation(summary = "Sterge un utilizator")
    @ApiResponse(responseCode = "200", description = "OK.")
    @ApiResponse(responseCode = "202", description = "Accepted.")
    @ApiResponse(responseCode = "204", description = "No Content. Utilizatorul a fost sters.")
    @ApiResponse(responseCode = "404", description = "Not Found. Utilizatorul nu a fost gasit.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Integer id,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.ADMIN
        );

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}