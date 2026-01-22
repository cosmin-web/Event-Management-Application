package com.example.clientservice.infrastructure.adapter.out.event.dto;

public class PackageShort {
    private Integer id;
    private String nume;
    private String locatie;
    private String descriere;
    private Integer numarLocuri;

    public PackageShort() {}

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
