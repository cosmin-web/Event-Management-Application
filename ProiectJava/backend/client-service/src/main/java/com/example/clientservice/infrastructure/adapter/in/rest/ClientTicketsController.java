package com.example.clientservice.infrastructure.adapter.in.rest;

import com.example.clientservice.application.auth.AuthenticatedUser;
import com.example.clientservice.application.auth.AuthorizationService;
import com.example.clientservice.application.auth.ServiceTokenProvider;
import com.example.clientservice.application.service.ClientService;
import com.example.clientservice.domain.model.UserRole;
import com.example.clientservice.infrastructure.adapter.out.event.dto.TicketData;
import com.example.clientservice.application.service.ClientTicketsService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/client-service/clients")
@Tag(name = "Client Tickets", description = "Operatii pentru validarea si listarea biletelor")
public class ClientTicketsController {

    @Autowired
    private ClientTicketsService ticketsService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private ServiceTokenProvider serviceTokenProvider;

    @Autowired
    private ClientService clientService;

    private EntityModel<TicketData> toModel(TicketData data, String email) {
        EntityModel<TicketData> model = EntityModel.of(data);
        model.add(linkTo(ClientTicketsController.class).slash(email).slash("tickets").withRel("parent"));
        return model;
    }

    @Operation(summary = "Valideaza un bilet")
    @ApiResponse(responseCode = "200", description = "Bilet valid.")
    @ApiResponse(responseCode = "201", description = "Validat.")
    @ApiResponse(responseCode = "409", description = "Conflict.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Content.")
    @PostMapping("/{email}/tickets/validate")
    public ResponseEntity<EntityModel<TicketData>> validateTicket(
            @PathVariable String email,
            @RequestParam String cod,
            @RequestParam(defaultValue = "false") boolean save,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserRole.CLIENT, UserRole.ADMIN
        );

        var client = clientService.findByEmail(email).orElse(null);

        if (current.getRole() == UserRole.CLIENT) {
            if (client == null || !email.equalsIgnoreCase(current.getEmail())) {
                return ResponseEntity.status(403).build();
            }
        }

        return ticketsService.validateTicket(email, cod, save, authorizationHeader)
                .map(data -> toModel(data, email))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @Operation(summary = "Lista paginata de bilete")
    @ApiResponse(responseCode = "200", description = "Lista returnata.")
    @ApiResponse(responseCode = "404", description = "Nu a fost gasit.")
    @GetMapping("/{email}/tickets")
    public ResponseEntity<PagedModel<EntityModel<TicketData>>> getClientTickets(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5", name = "items_per_page") int size,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            PagedResourcesAssembler<TicketData> assembler) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserRole.CLIENT, UserRole.ADMIN
        );

        var clientOpt = clientService.findByEmail(email);

        if (clientOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (current.getRole() == UserRole.CLIENT && !email.equalsIgnoreCase(current.getEmail())) {
            return ResponseEntity.status(403).build();
        }

        List<TicketData> all = ticketsService.listDetailedTickets(email, authorizationHeader);
        int start = page * size;
        int end = Math.min(start + size, all.size());
        List<TicketData> paginated = start >= all.size() ? List.of() : all.subList(start, end);

        PageImpl<TicketData> pageObj = new PageImpl<>(paginated, PageRequest.of(page, size), all.size());
        PagedModel<EntityModel<TicketData>> pagedModel = assembler.toModel(pageObj, entity -> toModel(entity, email));
        pagedModel.add(linkTo(ClientTicketsController.class).slash(email).slash("tickets").withRel("parent"));

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Cumpara bilet eveniment")
    @ApiResponse(responseCode = "201", description = "Creat.")
    @ApiResponse(responseCode = "409", description = "Conflict.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Content.")
    @PostMapping("/{email}/tickets/events/{eventId}")
    public ResponseEntity<EntityModel<TicketData>> buyEventTicket(
            @PathVariable String email,
            @PathVariable Integer eventId,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserRole.CLIENT, UserRole.ADMIN
        );

        var client = clientService.findByEmail(email).orElse(null);
        if(current.getRole() == UserRole.CLIENT) {
            if(client == null || !email.equalsIgnoreCase(client.getEmail())) {
                return ResponseEntity.status(403).build();
            }
        }

        String serviceToken = serviceTokenProvider.getServiceToken();
        TicketData data = ticketsService.buyTicketForEvent(email, eventId, serviceToken);
        EntityModel<TicketData> model = toModel(data, email);

        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Cumpara bilet pachet")
    @ApiResponse(responseCode = "201", description = "Creat.")
    @ApiResponse(responseCode = "409", description = "Conflict.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Content.")
    @PostMapping("/{email}/tickets/packages/{packageId}")
    public ResponseEntity<EntityModel<TicketData>> buyPackageTicket(
            @PathVariable String email,
            @PathVariable Integer packageId,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserRole.CLIENT, UserRole.ADMIN
        );

        var client = clientService.findByEmail(email).orElse(null);
        if(current.getRole() == UserRole.CLIENT) {
            if(client == null || !email.equalsIgnoreCase(client.getEmail())) {
                return ResponseEntity.status(403).build();
            }
        }

        String serviceToken = serviceTokenProvider.getServiceToken();
        TicketData data = ticketsService.buyTicketForPackage(email, packageId, serviceToken);
        EntityModel<TicketData> model = toModel(data, email);

        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Sterge bilet")
    @ApiResponse(responseCode = "200", description = "OK.")
    @ApiResponse(responseCode = "202", description = "Accepted.")
    @ApiResponse(responseCode = "204", description = "No Content.")
    @ApiResponse(responseCode = "404", description = "Not Found.")
    @DeleteMapping("/tickets/{cod}")
    public ResponseEntity<Void> deleteTicketEverywhere(
            @PathVariable String cod,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserRole.ADMIN, UserRole.OWNER_EVENT
        );

        try {
            ticketsService.deleteTicketEverywhere(cod, authorizationHeader);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}