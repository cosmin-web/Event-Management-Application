package com.example.eventservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public class EventDTO {

    private Integer id;

    @NotBlank(message = "Numele evenimentului este obligatoriu.")
    @Size(max = 100, message = "Numele evenimentului nu poate depasi 100 de caractere.")
    private String nume;

    @NotBlank(message = "Locatia este obligatorie.")
    @Size(max = 150, message = "Locatia nu poate depasi 150 de caractere.")
    private String locatie;

    @Size(max = 500, message = "Descrierea este prea lunga (maxim 500 caractere).")
    private String descriere;

    @NotNull(message = "Trebuie sa specificati numarul de locuri.")
    @Min(value = 1, message = "Trebuie sa existe cel putin un loc disponibil.")
    private Integer numarLocuri;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer ownerId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String ownerEmail;

    private Integer ticketsSold;
    private Integer availableTickets;

    public EventDTO() {}

    public EventDTO(Integer id, String nume, String locatie, String descriere, Integer numarLocuri, Integer ownerId) {
        this.id = id;
        this.nume = nume;
        this.locatie = locatie;
        this.descriere = descriere;
        this.numarLocuri = numarLocuri;
        this.ownerId = ownerId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public String getLocatie() { return locatie; }
    public void setLocatie(String locatie) { this.locatie = locatie; }

    public String getDescriere() { return descriere; }
    public void setDescriere(String descriere) { this.descriere = descriere; }

    public Integer getNumarLocuri() { return numarLocuri; }
    public void setNumarLocuri(Integer numarLocuri) { this.numarLocuri = numarLocuri; }

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer ownerId) { this.ownerId = ownerId; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public Integer getTicketsSold() { return ticketsSold; }
    public void setTicketsSold(Integer ticketsSold) { this.ticketsSold = ticketsSold; }

    public Integer getAvailableTickets() { return availableTickets; }
    public void setAvailableTickets(Integer availableTickets) { this.availableTickets = availableTickets; }
}