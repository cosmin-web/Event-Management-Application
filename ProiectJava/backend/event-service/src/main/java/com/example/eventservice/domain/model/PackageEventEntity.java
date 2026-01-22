package com.example.eventservice.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pachete_evenimente")
@IdClass(PackageEventIdEntity.class)
public class PackageEventEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "pachet_id", nullable = false)
    private PackageEntity pachet;

    @Id
    @ManyToOne
    @JoinColumn(name = "eveniment_id", nullable = false)
    private EventEntity eveniment;

    public PackageEventEntity() {}

    public PackageEventEntity(PackageEntity pachet, EventEntity eveniment) {
        this.pachet = pachet;
        this.eveniment = eveniment;
    }

    public PackageEntity getPachet() { return pachet; }
    public void setPachet(PackageEntity pachet) { this.pachet = pachet; }

    public EventEntity getEveniment() { return eveniment; }
    public void setEveniment(EventEntity eveniment) { this.eveniment = eveniment; }
}