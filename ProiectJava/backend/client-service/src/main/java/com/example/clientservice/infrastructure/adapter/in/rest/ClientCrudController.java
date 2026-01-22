package com.example.clientservice.infrastructure.adapter.in.rest;

import com.example.clientservice.application.auth.AuthenticatedUser;
import com.example.clientservice.application.auth.AuthorizationService;
import com.example.clientservice.domain.model.UserRole;
import com.example.clientservice.application.dto.ClientDTO;
import com.example.clientservice.application.mapper.ClientMapper;
import com.example.clientservice.domain.model.ClientDocument;
import com.example.clientservice.application.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/client-service/clients")
@Tag(name = "Clients", description = "Operatii pentru gestionarea clientilor")
public class ClientCrudController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private AuthorizationService authorizationService;

    static class EmailSyncRequest {
        public String oldEmail;
        public String newEmail;
    }

    private EntityModel<ClientDTO> toModel(ClientDTO dto) {
        EntityModel<ClientDTO> model = EntityModel.of(dto);
        if (dto.getId() != null) {
            model.add(linkTo(methodOn(ClientCrudController.class).getClientById(dto.getId(), null)).withSelfRel());
        }
        model.add(linkTo(ClientCrudController.class).withRel("parent"));
        return model;
    }

    @Operation(summary = "Listare clienti")
    @ApiResponse(responseCode = "200", description = "Lista de clienti a fost returnata.")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ClientDTO>>> getClients(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5", name = "items_per_page") int size,
            PagedResourcesAssembler<ClientDTO> assembler) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserRole.ADMIN
        );

        List<ClientDocument> all = clientService.findAlls(name);

        int start = page * size;
        int end = Math.min(start + size, all.size());
        List<ClientDocument> sublist = start >= all.size() ? List.of() : all.subList(start, end);

        List<ClientDTO> dtos = sublist.stream()
                .map(ClientMapper::toDTO)
                .collect(Collectors.toList());

        PageImpl<ClientDTO> pageObj = new PageImpl<>(dtos, PageRequest.of(page, size), all.size());
        PagedModel<EntityModel<ClientDTO>> pagedModel = assembler.toModel(pageObj, this::toModel);
        pagedModel.add(linkTo(ClientCrudController.class).withRel("parent"));

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Obtine un client dupa ID")
    @ApiResponse(responseCode = "200", description = "Clientul a fost gasit.")
    @ApiResponse(responseCode = "404", description = "Clientul nu exista.")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ClientDTO>> getClientById(
            @PathVariable String id,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserRole.ADMIN, UserRole.CLIENT, UserRole.OWNER_EVENT
        );

        return clientService.findById(id)
                .map(client -> {
                    if (current.getRole() != UserRole.ADMIN && !client.getEmail().equalsIgnoreCase(current.getEmail())) {
                        throw new RuntimeException("Access Denied");
                    }
                    return ResponseEntity.ok(toModel(ClientMapper.toDTO(client)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cauta client dupa Email")
    @ApiResponse(responseCode = "200", description = "Clientul a fost gasit.")
    @ApiResponse(responseCode = "404", description = "Clientul nu exista.")
    @GetMapping("/email/{email}")
    public ResponseEntity<EntityModel<ClientDTO>> getClientByEmail(
            @PathVariable String email,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserRole.ADMIN, UserRole.CLIENT, UserRole.OWNER_EVENT
        );

        if (current.getRole() != UserRole.ADMIN && !email.equalsIgnoreCase(current.getEmail())) {
            return ResponseEntity.status(403).build();
        }

        return clientService.findByEmail(email)
                .map(client -> ResponseEntity.ok(toModel(ClientMapper.toDTO(client))))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Creeaza sau actualizeaza un client")
    @ApiResponse(responseCode = "201", description = "Client creat sau actualizat.")
    @ApiResponse(responseCode = "409", description = "Conflict (ex: constrangere de unicitate incalcata).")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Content (date invalide).")
    @PostMapping
    public ResponseEntity<EntityModel<ClientDTO>> createOrUpdateByEmail(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody @Valid ClientDTO dto) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserRole.ADMIN, UserRole.CLIENT, UserRole.OWNER_EVENT
        );

        if (current.getRole() != UserRole.ADMIN && !dto.getEmail().equalsIgnoreCase(current.getEmail())) {
            return ResponseEntity.status(403).build();
        }

        ClientDocument saved = clientService.updateByEmail(ClientMapper.toDocument(dto));
        EntityModel<ClientDTO> model = toModel(ClientMapper.toDTO(saved));

        try {
            return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
        } catch (Exception e) {
            return ResponseEntity.status(201).body(model);
        }
    }

    @Operation(summary = "Sincronizare email (Admin only) - PUT")
    @ApiResponse(responseCode = "204", description = "No Content - Email actualizat cu succes.")
    @ApiResponse(responseCode = "201", description = "Created (in contextul PUT, daca resursa ar fi creata).")
    @ApiResponse(responseCode = "409", description = "Conflict.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Content.")
    @PutMapping("/sync-email")
    public ResponseEntity<Void> syncEmail(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody EmailSyncRequest request) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader, UserRole.ADMIN, UserRole.OWNER_EVENT
        );

        clientService.updateClientEmail(request.oldEmail, request.newEmail);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Sterge un client")
    @ApiResponse(responseCode = "200", description = "OK.")
    @ApiResponse(responseCode = "202", description = "Accepted.")
    @ApiResponse(responseCode = "204", description = "No Content.")
    @ApiResponse(responseCode = "404", description = "Not Found).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(
            @PathVariable String id,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader, UserRole.ADMIN
        );

        if (clientService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        clientService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}