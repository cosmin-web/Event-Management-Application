package com.example.clientservice.infrastructure.adapter.in.rest;

import com.example.clientservice.application.auth.AuthenticatedUser;
import com.example.clientservice.application.auth.AuthorizationService;
import com.example.clientservice.domain.model.UserRole;
import com.example.clientservice.application.mapper.ClientMapper;
import com.example.clientservice.domain.model.ClientDocument;
import com.example.clientservice.application.service.ClientService;
import com.example.clientservice.application.dto.PublicClientDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/client-service/clients/public")
@Tag(name = "Public Clients", description = "Acces public la informatii despre clienti care au bilete")
public class ClientPublicController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private AuthorizationService authorizationService;

    private EntityModel<PublicClientDTO> toPublicModel(PublicClientDTO dto, String type, Integer id) {
        EntityModel<PublicClientDTO> model = EntityModel.of(dto);

        if ("event".equals(type)) {
            model.add(linkTo(ClientPublicController.class).slash("by-event").slash(id).slash(dto.getId()).withSelfRel());
            model.add(linkTo(ClientPublicController.class).slash("by-event").slash(id).withRel("parent"));
        } else {
            model.add(linkTo(ClientPublicController.class).slash("by-package").slash(id).slash(dto.getId()).withSelfRel());
            model.add(linkTo(ClientPublicController.class).slash("by-package").slash(id).withRel("parent"));
        }
        return model;
    }

    @Operation(summary = "Lista paginata de clienti publici la un eveniment")
    @ApiResponse(responseCode = "200", description = "Lista de clienti a fost returnata.")
    @ApiResponse(responseCode = "404", description = "Evenimentul nu a fost gasit.")
    @GetMapping("/by-event/{eventId}")
    public ResponseEntity<PagedModel<EntityModel<PublicClientDTO>>> getClientsByEvent(
            @PathVariable Integer eventId,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5", name = "items_per_page") int size,
            PagedResourcesAssembler<PublicClientDTO> assembler) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserRole.ADMIN, UserRole.OWNER_EVENT
        );

        List<ClientDocument> all = clientService.findClientsByEventId(eventId).stream()
                .filter(ClientDocument::isPublic)
                .toList();

        int start = page * size;
        int end = Math.min(start + size, all.size());
        List<ClientDocument> sublist = start >= all.size() ? List.of() : all.subList(start, end);

        List<PublicClientDTO> dtos = sublist.stream().map(ClientMapper::toPublicDTO).collect(Collectors.toList());
        PageImpl<PublicClientDTO> pageObj = new PageImpl<>(dtos, PageRequest.of(page, size), all.size());

        PagedModel<EntityModel<PublicClientDTO>> pagedModel = assembler.toModel(pageObj, dto -> toPublicModel(dto, "event", eventId));
        pagedModel.add(linkTo(ClientPublicController.class).slash("by-event").slash(eventId).withRel("parent"));

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Lista paginata de clienti publici la un pachet")
    @ApiResponse(responseCode = "200", description = "Lista de clienti a fost returnata.")
    @ApiResponse(responseCode = "404", description = "Pachetul nu a fost gasit.")
    @GetMapping("/by-package/{packageId}")
    public ResponseEntity<PagedModel<EntityModel<PublicClientDTO>>> getClientsByPackage(
            @PathVariable Integer packageId,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5", name = "items_per_page") int size,
            PagedResourcesAssembler<PublicClientDTO> assembler) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserRole.ADMIN, UserRole.OWNER_EVENT
        );

        List<ClientDocument> all = clientService.findClientsByPackageId(packageId).stream()
                .filter(ClientDocument::isPublic)
                .toList();

        int start = page * size;
        int end = Math.min(start + size, all.size());
        List<ClientDocument> sublist = start >= all.size() ? List.of() : all.subList(start, end);

        List<PublicClientDTO> dtos = sublist.stream().map(ClientMapper::toPublicDTO).collect(Collectors.toList());
        PageImpl<PublicClientDTO> pageObj = new PageImpl<>(dtos, PageRequest.of(page, size), all.size());

        PagedModel<EntityModel<PublicClientDTO>> pagedModel = assembler.toModel(pageObj, dto -> toPublicModel(dto, "package", packageId));
        pagedModel.add(linkTo(ClientPublicController.class).slash("by-package").slash(packageId).withRel("parent"));

        return ResponseEntity.ok(pagedModel);
    }
}