package com.example.eventservice.infrastructure.adapter.in.rest;

import com.example.eventservice.application.auth.AuthenticatedUser;
import com.example.eventservice.application.auth.AuthorizationService;
import com.example.eventservice.application.dto.EventDTO;
import com.example.eventservice.application.mapper.EventMapper;
import com.example.eventservice.domain.model.EventEntity;
import com.example.eventservice.domain.model.UserEntity;
import com.example.eventservice.application.service.EventService;
import com.example.eventservice.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/event-manager/events")
@Tag(name = "Events", description = "Operatii pentru gestionarea evenimentelor")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthorizationService authorizationService;

    private EntityModel<EventDTO> toModel(EventEntity event) {
        EventDTO dto = enrichEvent(event);
        EntityModel<EventDTO> model = EntityModel.of(dto);

        model.add(linkTo(methodOn(EventController.class).getEventById(event.getId(), null)).withSelfRel());

        model.add(linkTo(EventController.class).withRel("parent"));

        return model;
    }

    private EventDTO enrichEvent(EventEntity event) {
        EventDTO dto = EventMapper.fromEntity(event);

        int soldOnEvent = eventService.countTicketsSold(event);
        int packageImpact = eventService.countPackageTicketsImpactForEvent(event);

        int capacity = event.getNumarLocuri() != null ? event.getNumarLocuri() : 0;
        int available = capacity - soldOnEvent - packageImpact;

        dto.setTicketsSold(soldOnEvent);
        dto.setAvailableTickets(Math.max(available, 0));

        if (event.getOwner() != null)
            dto.setOwnerEmail(event.getOwner().getEmail());

        return dto;
    }

    @Operation(summary = "Listare evenimente", description = "Returneaza o lista paginata de evenimente.")
    @ApiResponse(responseCode = "200", description = "Lista de evenimente a fost returnata.")
    @ApiResponse(responseCode = "404", description = "Not Found. Resursa nu a fost gasita.")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<EventDTO>>> getEvents(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, name = "available_tickets") Integer availableTickets,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5", name = "items_per_page") int size,
            PagedResourcesAssembler<EventEntity> assembler) {

//        AuthenticatedUser current = authorizationService.requireUser(
//                authorizationHeader,
//                UserEntity.Role.ADMIN,
//                UserEntity.Role.OWNER_EVENT,
//                UserEntity.Role.CLIENT
//        );

        var resultPage = eventService.searchEvents(name, location, availableTickets, page, size);

        PagedModel<EntityModel<EventDTO>> pagedModel = assembler.toModel(resultPage, this::toModel);

        // Parent link la nivel de colectie (pentru Bruno/navigare inapoi la root daca e cazul, sau self curat)
        // Aici am pus link catre controller root ca parent generic
        pagedModel.add(linkTo(EventController.class).withRel("parent"));

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Obtine un eveniment dupa ID")
    @ApiResponse(responseCode = "200", description = "Evenimentul a fost gasit.")
    @ApiResponse(responseCode = "404", description = "Evenimentul nu a fost gasit.")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<EventDTO>> getEventById(
            @PathVariable Integer id,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

//        AuthenticatedUser current = authorizationService.requireUser(
//                authorizationHeader,
//                UserEntity.Role.ADMIN,
//                UserEntity.Role.OWNER_EVENT,
//                UserEntity.Role.CLIENT
//        );

        return eventService.getEventById(id)
                .map(this::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Creeaza un eveniment nou")
    @ApiResponse(responseCode = "201", description = "Created. Evenimentul a fost creat.")
    @ApiResponse(responseCode = "409", description = "Conflict. Exista un eveniment cu acest nume.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Content. Date invalide.")
    @PostMapping
    public ResponseEntity<EntityModel<EventDTO>> createEvent(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody @Valid EventDTO dto) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.OWNER_EVENT
        );

        Integer ownerId = current.getUserId();

        UserEntity owner = userService.getUserById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Proprietarul nu exista."));

        dto.setOwnerId(ownerId);

        EventEntity event = EventMapper.toEntity(dto, owner);
        EventEntity saved = eventService.createEvent(event);

        EntityModel<EventDTO> model = toModel(saved);
        URI location = model.getRequiredLink("self").toUri();

        return ResponseEntity.created(location).body(model);
    }


    @Operation(summary = "Actualizeaza un eveniment existent")
    @ApiResponse(responseCode = "201", description = "Created.")
    @ApiResponse(responseCode = "204", description = "No Content.")
    @ApiResponse(responseCode = "409", description = "Conflict.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Content.")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<EventDTO>> updateEvent(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @PathVariable Integer id,
            @RequestBody @Valid EventDTO dto) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.OWNER_EVENT
        );

        EventEntity existing = eventService.getEventById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evenimentul nu exista."));

        if (existing.getOwner() == null ||
                existing.getOwner().getId() == null ||
                !existing.getOwner().getId().equals(current.getUserId())) {
            return ResponseEntity.status(403).build();
        }

        UserEntity owner = existing.getOwner();
        dto.setOwnerId(owner.getId());

        EventEntity entity = EventMapper.toEntity(dto, owner);
        EventEntity updated = eventService.updateEvent(id, entity);

        return ResponseEntity.ok(toModel(updated));
    }


    @Operation(summary = "Sterge un eveniment dupa ID")
    @ApiResponse(responseCode = "200", description = "OK.")
    @ApiResponse(responseCode = "202", description = "Accepted.")
    @ApiResponse(responseCode = "204", description = "No Content. Evenimentul a fost sters.")
    @ApiResponse(responseCode = "404", description = "Not Found. Evenimentul nu a fost gasit.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @PathVariable Integer id) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.OWNER_EVENT
        );

        EventEntity existing = eventService.getEventById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evenimentul nu exista."));

        if (existing.getOwner() == null ||
                existing.getOwner().getId() == null ||
                !existing.getOwner().getId().equals(current.getUserId())) {
            return ResponseEntity.status(403).build();
        }

        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}