package com.example.eventservice.application.mapper;

import com.example.eventservice.application.dto.PackageEventDTO;
import com.example.eventservice.domain.model.EventEntity;
import com.example.eventservice.domain.model.PackageEntity;
import com.example.eventservice.domain.model.PackageEventEntity;

public class PackageEventMapper {

    public static PackageEventDTO fromEntity(PackageEventEntity entity) {
        PackageEventDTO dto = new PackageEventDTO();
        dto.setEventId(entity.getEveniment().getId());
        dto.setPackageId(entity.getPachet().getId());

        //adaugare campuri noi
        dto.setPackageName(entity.getPachet().getNume());
        dto.setEventName(entity.getEveniment().getNume());
        dto.setEventLocation(entity.getEveniment().getLocatie());

        return dto;
    }

    public static PackageEventEntity toEntity(PackageEventDTO dto, PackageEntity pachet, EventEntity eveniment) {
        PackageEventEntity pe = new PackageEventEntity();
        pe.setEveniment(eveniment);
        pe.setPachet(pachet);
        return pe;
    }
}
