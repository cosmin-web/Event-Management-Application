package com.example.eventservice.infrastructure.adapter.in.rest;

import com.example.eventservice.application.auth.AuthenticatedUser;
import com.example.eventservice.application.dto.PackageDTO;
import com.example.eventservice.application.mapper.PackageMapper;
import com.example.eventservice.application.service.PackageService;
import com.example.eventservice.application.service.UserService;
import com.example.eventservice.application.auth.AuthorizationService;
import com.example.eventservice.domain.model.PackageEntity;
import com.example.eventservice.domain.model.UserEntity;
import com.example.eventservice.domain.repository.TicketRepository;
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
@RequestMapping("/api/event-manager/event-packets")
@Tag(name="Packages", description = "Operatii pentru gestionarea pachetelor de evenimente.")
public class PackageController {

    @Autowired
    private PackageService packageService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private TicketRepository ticketRepository;

    private EntityModel<PackageDTO> toModel(PackageEntity entity) {
        PackageDTO dto = enrichPackage(entity);
        EntityModel<PackageDTO> model = EntityModel.of(dto);

        model.add(linkTo(methodOn(PackageController.class).getPackageById(entity.getId(), null)).withSelfRel());

        model.add(linkTo(PackageController.class).withRel("parent"));

        return model;
    }

    private PackageDTO enrichPackage(PackageEntity entity) {
        PackageDTO dto = PackageMapper.fromEntity(entity);

        if (entity.getOwner() != null) {
            dto.setOwnerEmail(entity.getOwner().getEmail());
        }

        int countEvents = packageService.countEventsInPackage(entity);
        dto.setNumberOfEvents(countEvents);

        int countAvailableTickets = packageService.calculeazaLocuriDisponibile(entity);
        dto.setAvailableTickets(countAvailableTickets);

        int realSales = ticketRepository.findByPachet(entity).size();
        dto.setTicketsSold(realSales);

        return dto;
    }

    @Operation(summary = "Listare pachete", description = "Returneaza o lista paginata de pachete.")
    @ApiResponse(responseCode = "200", description = "Ok. Lista de pachete a fost returnata.")
    @ApiResponse(responseCode = "404", description = "Not Found. Resursa nu a fost gasita.")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<PackageDTO>>> getPackages(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String eventName,
            @RequestParam(required = false, name = "available_tickets") Integer availableTickets,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5", name = "items_per_page") int size,
            PagedResourcesAssembler<PackageEntity> assembler) {

//        AuthenticatedUser current = authorizationService.requireUser(
//                authorizationHeader,
//                UserEntity.Role.ADMIN,
//                UserEntity.Role.OWNER_EVENT,
//                UserEntity.Role.CLIENT
//        );

        var resultPage = packageService.searchPackages(name, type, eventName, availableTickets, page, size);

        PagedModel<EntityModel<PackageDTO>> pagedModel = assembler.toModel(resultPage, this::toModel);

        pagedModel.add(linkTo(PackageController.class).withRel("parent"));

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Obtine un pachet dupa ID")
    @ApiResponse(responseCode = "200", description = "Ok. Pachetul a fost gasit.")
    @ApiResponse(responseCode = "404", description = "Not Found. Pachetul nu a fost gasit.")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PackageDTO>> getPackageById(
            @PathVariable Integer id,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.ADMIN,
                UserEntity.Role.OWNER_EVENT,
                UserEntity.Role.CLIENT
        );

        return packageService.getPackageById(id)
                .map(this::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Creeaza un pachet nou")
    @ApiResponse(responseCode = "201", description = "Created. Pachetul a fost creat cu succes.")
    @ApiResponse(responseCode = "409", description = "Conflict. Exista deja un pachet cu acelasi nume.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Content. Date invalide.")
    @PostMapping
    public ResponseEntity<EntityModel<PackageDTO>> createPackage(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody @Valid PackageDTO dto) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.OWNER_EVENT
        );

        Integer ownerId = current.getUserId();

        UserEntity owner = userService.getUserById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Proprietarul nu exista"));

        dto.setOwnerId(ownerId);

        PackageEntity entity = PackageMapper.toEntity(dto, owner);
        PackageEntity saved = packageService.createPackages(entity);

        EntityModel<PackageDTO> model = toModel(saved);
        URI location = model.getRequiredLink("self").toUri();

        return ResponseEntity.created(location).body(model);
    }

    @Operation(summary = "Actualizeaza un pachet existent")
    @ApiResponse(responseCode = "201", description = "Created.")
    @ApiResponse(responseCode = "204", description = "No Content.")
    @ApiResponse(responseCode = "409", description = "Conflict.")
    @ApiResponse(responseCode = "415", description = "Unsupported Media Type.")
    @ApiResponse(responseCode = "422", description = "Unprocessable Content.")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<PackageDTO>> updatePackage(
            @PathVariable Integer id,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody @Valid PackageDTO dto) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.OWNER_EVENT
        );

        PackageEntity existing = packageService.getPackageById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pachetul nu exista."));

        if (existing.getOwner() == null ||
                existing.getOwner().getId() == null ||
                !existing.getOwner().getId().equals(current.getUserId())) {
            return ResponseEntity.status(403).build();
        }

        UserEntity owner = existing.getOwner();
        dto.setOwnerId(owner.getId());

        PackageEntity entity = PackageMapper.toEntity(dto, owner);
        PackageEntity updated = packageService.updatePackage(id, entity);

        return ResponseEntity.ok(toModel(updated));
    }

    @Operation(summary = "Sterge un pachet")
    @ApiResponse(responseCode = "200", description = "OK.")
    @ApiResponse(responseCode = "202", description = "Accepted.")
    @ApiResponse(responseCode = "204", description = "No Content. Pachetul a fost sters.")
    @ApiResponse(responseCode = "404", description = "Not Found. Pachetul nu a fost gasit.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(
            @PathVariable Integer id,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {

        AuthenticatedUser current = authorizationService.requireUser(
                authorizationHeader,
                UserEntity.Role.OWNER_EVENT
        );

        PackageEntity existing = packageService.getPackageById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pachetul nu exista."));

        if (existing.getOwner() == null ||
                existing.getOwner().getId() == null ||
                !existing.getOwner().getId().equals(current.getUserId())) {
            return ResponseEntity.status(403).build();
        }

        packageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }
}