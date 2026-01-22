package com.example.eventservice.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "evenimente")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_owner", nullable = false)
    private UserEntity owner;

    @Column(nullable = false, unique = true, length = 100)
    private String nume;

    @Column(length = 150)
    private String locatie;

    @Column(length = 500)
    private String descriere;

    @Column(name = "numarLocuri")
    private Integer numarLocuri;

    public EventEntity() {}

    public EventEntity(Integer id, UserEntity owner, String nume, String locatie, String descriere, Integer numarLocuri) {
        this.id = id;
        this.owner = owner;
        this.nume = nume;
        this.locatie = locatie;
        this.descriere = descriere;
        this.numarLocuri = numarLocuri;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public UserEntity getOwner() { return owner; }
    public void setOwner(UserEntity owner) { this.owner = owner; }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public String getLocatie() { return locatie; }
    public void setLocatie(String locatie) { this.locatie = locatie; }

    public String getDescriere() { return descriere; }
    public void setDescriere(String descriere) { this.descriere = descriere; }

    public Integer getNumarLocuri() { return numarLocuri; }
    public void setNumarLocuri(Integer numarLocuri) { this.numarLocuri = numarLocuri; }
}