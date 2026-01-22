package com.example.eventservice.infrastructure.adapter.in.rest;

import com.example.eventservice.application.auth.AuthenticatedUser;
import com.example.eventservice.application.dto.PackageEventDTO;
import com.example.eventservice.application.auth.AuthorizationService;
import com.example.eventservice.domain.model.EventEntity;
import com.example.eventservice.domain.model.PackageEntity;
import com.example.eventservice.domain.model.PackageEventEntity;
import com.example.eventservice.application.service.EventService;
import com.example.eventservice.application.service.PackageEventService;
import com.example.eventservice.application.service.PackageService;
import com.example.eventservice.domain.model.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/event-manager")
@Tag(name = "Package-Event Relations", description = "Operatii pentru gestionarea relatiilor dintre evenimente si pachete")
public class PackageEventController {

    @Autowired
    private PackageEventService packageEventService;

    @Autowired
    private PackageService packageService;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuthorizationService authorizationService;

    private EntityModel<PackageEventDTO> toModel(PackageEventDTO dto) {
        EntityModel<PackageEventDTO> model = EntityModel.of(dto);

        model.add(linkTo(methodOn(PackageEventController.class)
                .getEventsForPackage(dto.getPackageId(), null))
                .withRel("package-events"));

        model.add(linkTo(methodOn(PackageEventController.class)
                .getPackagesForEvent(dto.getEventId(), null))
                .withRel("event-packages"));

        model.add(linkTo(PackageController.class).withRel("parent"));

        return model;
    }

    private PackageEventDTO enrichRelation(PackageEventEntity relation) {
        PackageEventDTO dto = new PackageEventDTO();

        var pachet = relation.getPachet();
        var event = relation.getEveniment();

        dto.setPackageId(pachet.getId());
        dto.setEventId(event.getId());

        dto.setPackageName(pachet.getNume());
        dto.setEventName(event.getNume());
        dto.setEventLocation(event.getLocatie());

        dto.setEventDescription(event.getDescriere());

        return dto;
    }


    @Operation(summary = "Listare pachete pentru un eveniment")
    @ApiResponse(responseCode = "200", description = "Ok. Lista a fost returnata.")
    @ApiResponse(responseCode = "404", description = "Not Found. Evenimentul nu exista.")
    @GetMapping("/events/{eventId}/event-packets")
    public ResponseEntity<CollectionModel<EntityModel<PackageEventDTO>>> getPackagesForEvent(
            @PathVariable Integer eventId,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.ADMIN,
                UserEntity.Role.OWNER_EVENT,
                UserEntity.Role.CLIENT
        );

        EventEntity event = eventService.getEventById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evenimentul nu exista"));

        List<EntityModel<PackageEventDTO>> list = packageEventService.getPackagesForEvent(event).stream()
                .map(this::enrichRelation)
                .map(this::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(list));
    }

    @Operation(summary = "Listare evenimente dintr-un pachet")
    @ApiResponse(responseCode = "200", description = "Ok. Lista a fost returnata.")
    @ApiResponse(responseCode = "404", description = "Not Found. Pachetul nu exista.")
    @GetMapping("/event-packets/{packetId}/events")
    public ResponseEntity<CollectionModel<EntityModel<PackageEventDTO>>> getEventsForPackage(
            @PathVariable Integer packetId,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

//        AuthenticatedUser current = authorizationService.requireUser(
//                authorizationHeader,
//                UserEntity.Role.ADMIN,
//                UserEntity.Role.OWNER_EVENT,
//                UserEntity.Role.CLIENT
//        );

        PackageEntity pachet = packageService.getPackageById(packetId)
                .orElseThrow(() -> new IllegalArgumentException("Pachetul nu exista"));

        List<EntityModel<PackageEventDTO>> list = packageEventService.getEventsForPackage(pachet).stream()
                .map(this::enrichRelation)
                .map(this::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(list));
    }

    @Operation(summary = "Asociaza un eveniment unui pachet")
    @ApiResponse(responseCode = "201", description = "Created. Relatia a fost creata.")
    @ApiResponse(responseCode = "409", description = "Conflict. Evenimentul este deja asociat cu acest pachet.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Content. Date invalide.")
    @PostMapping("/event-packets/{packetId}/events/{eventId}")
    public ResponseEntity<EntityModel<PackageEventDTO>> createEventToPackage(
            @PathVariable Integer packetId,
            @PathVariable Integer eventId,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody(required = false) @Valid PackageEventDTO body) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.OWNER_EVENT
        );

        Integer ownerId = current.getUserId();

        PackageEntity pachet = packageService.getPackageById(packetId)
                .orElseThrow(() -> new IllegalArgumentException("Acest pachet nu exista."));

        EventEntity eveniment = eventService.getEventById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Acest eveniment nu exista."));

        if (pachet.getOwner() == null || pachet.getOwner().getId() == null ||
                eveniment.getOwner() == null || eveniment.getOwner().getId() == null ||
                !pachet.getOwner().getId().equals(ownerId) ||
                !eveniment.getOwner().getId().equals(ownerId)) {
            return ResponseEntity.status(403).build();
        }

        if (packageEventService.getRelation(pachet, eveniment).isPresent()) {
            throw new IllegalArgumentException("Evenimentul este deja asociat cu acest pachet.");
        }

        PackageEventEntity relation = packageEventService.addEventToPackage(pachet, eveniment);
        PackageEventDTO dto = enrichRelation(relation);
        EntityModel<PackageEventDTO> model = toModel(dto);

        URI location = linkTo(methodOn(PackageEventController.class).getEventsForPackage(packetId, null)).toUri();

        return ResponseEntity.created(location).body(model);
    }


    @Operation(summary = "Sterge un eveniment dintr-un pachet")
    @ApiResponse(responseCode = "200", description = "OK.")
    @ApiResponse(responseCode = "202", description = "Accepted.")
    @ApiResponse(responseCode = "204", description = "No Content. Relatia a fost stearsa cu succes.")
    @ApiResponse(responseCode = "404", description = "Not Found. Evenimentul sau pachetul nu a fost gasit.")
    @DeleteMapping("/event-packets/{packetId}/events/{eventId}")
    public ResponseEntity<Void> deleteEventFromPackage(
            @PathVariable Integer packetId,
            @PathVariable Integer eventId,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.OWNER_EVENT
        );

        Integer ownerId = current.getUserId();

        PackageEntity pachet = packageService.getPackageById(packetId)
                .orElseThrow(() -> new IllegalArgumentException("Acest pachet nu exista"));

        EventEntity eveniment = eventService.getEventById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Acest eveniment nu exista"));

        if (pachet.getOwner() == null || pachet.getOwner().getId() == null ||
                eveniment.getOwner() == null || eveniment.getOwner().getId() == null ||
                !pachet.getOwner().getId().equals(ownerId) ||
                !eveniment.getOwner().getId().equals(ownerId)) {
            return ResponseEntity.status(403).build();
        }

        packageEventService.removeEventFromPackage(pachet, eveniment);
        return ResponseEntity.noContent().build();
    }
}