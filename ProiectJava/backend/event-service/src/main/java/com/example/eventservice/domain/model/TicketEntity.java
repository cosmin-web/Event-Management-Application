package com.example.eventservice.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bilete")
public class TicketEntity {

    @Id
    private String cod;

    @ManyToOne
    @JoinColumn(name = "eveniment_id")
    private EventEntity eveniment;

    @ManyToOne
    @JoinColumn(name = "pachet_id")
    private PackageEntity pachet;

    public TicketEntity() {}

    public TicketEntity(String cod, PackageEntity pachet, EventEntity eveniment) {
        this.cod = cod;
        this.pachet = pachet;
        this.eveniment = eveniment;
    }

    public String getCod() { return cod; }
    public void setCod(String cod) { this.cod = cod; }

    public PackageEntity getPachet() { return pachet; }
    public void setPachet(PackageEntity pachet) { this.pachet = pachet; }

    public EventEntity getEveniment() { return eveniment; }
    public void setEveniment(EventEntity eveniment) { this.eveniment = eveniment; }
}