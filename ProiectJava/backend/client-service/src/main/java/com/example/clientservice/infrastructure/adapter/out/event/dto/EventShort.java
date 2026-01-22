package com.example.clientservice.infrastructure.adapter.out.event.dto;

public class EventShort {
    private Integer id;
    private String nume;
    private String locatie;
    private String descriere;
    private Integer numarLocuri;

    public EventShort() {
    }

    public EventShort(Integer id, String nume, String locatie, String descriere, Integer numarLocuri) {
        this.id = id;
        this.nume = nume;
        this.locatie = locatie;
        this.descriere = descriere;
        this.numarLocuri = numarLocuri;
    }

    public Integer getId() { return id; }
    public String getNume() { return nume; }
    public String getLocatie() { return locatie; }
    public String getDescriere() { return descriere; }
    public Integer getNumarLocuri() { return numarLocuri; }

    public void setId(Integer id) { this.id = id; }
    public void setNume(String nume) { this.nume = nume; }
    public void setLocatie(String locatie) { this.locatie = locatie; }
    public void setDescriere(String descriere) { this.descriere = descriere; }
    public void setNumarLocuri(Integer numarLocuri) { this.numarLocuri = numarLocuri; }
}
