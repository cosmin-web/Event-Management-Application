package com.example.eventservice.infrastructure.adapter.in.rest;

import com.example.eventservice.application.auth.AuthenticatedUser;
import com.example.eventservice.application.auth.AuthorizationService;
import com.example.eventservice.domain.model.EventEntity;
import com.example.eventservice.domain.model.PackageEntity;
import com.example.eventservice.domain.model.TicketEntity;
import com.example.eventservice.application.service.EventService;
import com.example.eventservice.application.service.PackageService;
import com.example.eventservice.application.service.TicketService;
import com.example.eventservice.domain.model.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/event-manager")
@Tag(name = "Tickets", description = "Operatii pentru gestionarea biletelor pentru evenimente si pachete")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private EventService eventService;

    @Autowired
    private PackageService packageService;

    @Autowired
    private AuthorizationService authorizationService;

    private EntityModel<Map<String, Object>> toModel(TicketEntity ticket) {
        Map<String, Object> data = enrichTicket(ticket);
        EntityModel<Map<String, Object>> model = EntityModel.of(data);

        model.add(linkTo(methodOn(TicketController.class).getTicketByCod(ticket.getCod(), null)).withSelfRel());
        model.add(linkTo(TicketController.class).slash("tickets").withRel("parent"));

        return model;
    }

    private Map<String, Object> enrichTicket(TicketEntity ticket) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cod", ticket.getCod());

        if (ticket.getEveniment() != null) {
            var e = ticket.getEveniment();
            data.put("event", Map.of(
                    "id", e.getId(),
                    "nume", e.getNume(),
                    "locatie", e.getLocatie(),
                    "descriere", e.getDescriere(),
                    "numarLocuri", e.getNumarLocuri()
            ));
        }

        if (ticket.getPachet() != null) {
            var p = ticket.getPachet();
            data.put("package", Map.of(
                    "id", p.getId(),
                    "nume", p.getNume(),
                    "locatie", p.getLocatie(),
                    "descriere", p.getDescriere(),
                    "numarLocuri", p.getNumarLocuri()
            ));
        }

        return data;
    }

    @Operation(summary = "Listare bilete")
    @ApiResponse(responseCode = "200", description = "Ok. Lista biletelor a fost returnata.")
    @ApiResponse(responseCode = "404", description = "Not Found. Resursa nu a fost gasita.")
    @GetMapping("/tickets")
    public ResponseEntity<PagedModel<EntityModel<Map<String, Object>>>> getTickets(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false) String eventName,
            @RequestParam(required = false) String packageName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5", name = "items_per_page") int size,
            PagedResourcesAssembler<TicketEntity> assembler) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.ADMIN,
                UserEntity.Role.OWNER_EVENT
        );

        var resultPage = ticketService.searchTickets(eventName, packageName, page, size);

        PagedModel<EntityModel<Map<String, Object>>> pagedModel = assembler.toModel(resultPage, this::toModel);
        pagedModel.add(linkTo(TicketController.class).slash("tickets").withRel("parent"));

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Obtine un bilet dupa cod")
    @ApiResponse(responseCode = "200", description = "Ok. Biletul a fost gasit.")
    @ApiResponse(responseCode = "404", description = "Not Found. Biletul nu a fost gasit.")
    @GetMapping("/tickets/{cod}")
    public ResponseEntity<EntityModel<Map<String, Object>>> getTicketByCod(
            @PathVariable String cod,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.ADMIN,
                UserEntity.Role.OWNER_EVENT,
                UserEntity.Role.CLIENT
        );

        return ticketService.getTicketByCode(cod)
                .map(this::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listare bilete pentru un eveniment")
    @ApiResponse(responseCode = "200", description = "Ok. Lista biletelor a fost returnata.")
    @ApiResponse(responseCode = "404", description = "Not Found. Evenimentul nu a fost gasit.")
    @GetMapping("/events/{eventId}/tickets")
    public ResponseEntity<CollectionModel<EntityModel<Map<String, Object>>>> getAllTicketsFromEvent(
            @PathVariable Integer eventId,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.ADMIN,
                UserEntity.Role.OWNER_EVENT
        );

        EventEntity eveniment = eventService.getEventById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evenimentul nu exista."));

        if (current.getRole() == UserEntity.Role.OWNER_EVENT) {
            if (eveniment.getOwner() == null ||
                    eveniment.getOwner().getId() == null ||
                    !eveniment.getOwner().getId().equals(current.getUserId())) {
                return ResponseEntity.status(403).build();
            }
        }

        List<EntityModel<Map<String, Object>>> list = ticketService.getTicketsByEvent(eveniment).stream()
                .map(this::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(list));
    }

    @Operation(summary = "Listare bilete pentru un pachet")
    @ApiResponse(responseCode = "200", description = "Ok. Lista biletelor a fost returnata.")
    @ApiResponse(responseCode = "404", description = "Not Found. Pachetul nu a fost gasit.")
    @GetMapping("/event-packets/{packetId}/tickets")
    public ResponseEntity<CollectionModel<EntityModel<Map<String, Object>>>> getAllTicketsFromPackage(
            @PathVariable Integer packetId,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.ADMIN,
                UserEntity.Role.OWNER_EVENT
        );

        PackageEntity pachet = packageService.getPackageById(packetId)
                .orElseThrow(() -> new IllegalArgumentException("Pachetul nu exista."));

        if (current.getRole() == UserEntity.Role.OWNER_EVENT) {
            if (pachet.getOwner() == null ||
                    pachet.getOwner().getId() == null ||
                    !pachet.getOwner().getId().equals(current.getUserId())) {
                return ResponseEntity.status(403).build();
            }
        }

        List<EntityModel<Map<String, Object>>> list = ticketService.getTicketsByPackage(pachet).stream()
                .map(this::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(list));
    }

    @Operation(summary = "Creeaza un bilet pentru un eveniment")
    @ApiResponse(responseCode = "201", description = "Created. Biletul a fost creat cu succes.")
    @ApiResponse(responseCode = "204", description = "No Content.")
    @ApiResponse(responseCode = "409", description = "Conflict.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Content. Nu mai sunt locuri disponibile.")
    @PutMapping("/events/{eventId}/tickets")
    public ResponseEntity<EntityModel<Map<String, Object>>> createTicketForEvent(
            @PathVariable Integer eventId,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.SERVICE_CLIENT
        );

        TicketEntity ticket = ticketService.createTicketForEvent(eventId);
        EntityModel<Map<String, Object>> model = toModel(ticket);
        URI location = model.getRequiredLink("self").toUri();

        return ResponseEntity.created(location).body(model);
    }

    @Operation(summary = "Creeaza un bilet pentru un pachet")
    @ApiResponse(responseCode = "201", description = "Created. Biletul a fost creat cu succes.")
    @ApiResponse(responseCode = "204", description = "No Content.")
    @ApiResponse(responseCode = "409", description = "Conflict.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Content. Nu mai sunt locuri disponibile.")
    @PutMapping("/event-packets/{packetId}/tickets")
    public ResponseEntity<EntityModel<Map<String, Object>>> createTicketForPackage(
            @PathVariable Integer packetId,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.SERVICE_CLIENT
        );

        TicketEntity ticket = ticketService.createTicketForPackage(packetId);
        EntityModel<Map<String, Object>> model = toModel(ticket);
        URI location = model.getRequiredLink("self").toUri();

        return ResponseEntity.created(location).body(model);
    }


    @Operation(summary = "Sterge un bilet dupa cod")
    @ApiResponse(responseCode = "200", description = "OK.")
    @ApiResponse(responseCode = "202", description = "Accepted.")
    @ApiResponse(responseCode = "204", description = "No Content. Biletul a fost sters.")
    @ApiResponse(responseCode = "404", description = "Not Found. Biletul nu a fost gasit.")
    @DeleteMapping("/tickets/{cod}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable String cod,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.ADMIN,
                UserEntity.Role.OWNER_EVENT
        );

        TicketEntity ticket = ticketService.getTicketByCode(cod)
                .orElseThrow(() -> new IllegalArgumentException("Biletul nu exista"));

        if (current.getRole() == UserEntity.Role.OWNER_EVENT) {
            Integer ownerId = current.getUserId();
            boolean allowed = false;

            EventEntity ev = ticket.getEveniment();
            if (ev != null && ev.getOwner() != null &&
                    ownerId.equals(ev.getOwner().getId())) {
                allowed = true;
            }

            PackageEntity pkg = ticket.getPachet();
            if (pkg != null && pkg.getOwner() != null &&
                    ownerId.equals(pkg.getOwner().getId())) {
                allowed = true;
            }

            if (!allowed) {
                return ResponseEntity.status(403).build();
            }
        }

        ticketService.deleteTicket(cod);
        return ResponseEntity.noContent().build();
    }
}