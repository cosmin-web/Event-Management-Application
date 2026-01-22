package com.example.clientservice.application.mapper;

import com.example.clientservice.application.dto.ClientDTO;
import com.example.clientservice.application.dto.PublicClientDTO;
import com.example.clientservice.application.dto.TicketDTO;
import com.example.clientservice.domain.model.ClientDocument;
import com.example.clientservice.domain.model.TicketRef;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClientMapper {

    public static ClientDTO toDTO(ClientDocument d) {
        ClientDTO dto = new ClientDTO();
        dto.setId(d.getIdAsString());
        dto.setEmail(d.getEmail());
        dto.setNume(d.getNume());
        dto.setPrenume(d.getPrenume());
        dto.setIsPublic(d.isPublic());
        dto.setSocial(d.getSocial());

        List<TicketRef> bilete = d.getBilete() != null ? d.getBilete() : Collections.emptyList();
        dto.setBilete(bilete.stream()
                .map(ClientMapper::toTicketDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    public static ClientDocument toDocument(ClientDTO dto) {
        ClientDocument d = new ClientDocument();
        d.setEmail(dto.getEmail());
        d.setNume(dto.getNume());
        d.setPrenume(dto.getPrenume());
        d.setPublic(dto.getIsPublic());
        d.setSocial(dto.getSocial());

        List<TicketDTO> bilete = dto.getBilete() != null ? dto.getBilete() : Collections.emptyList();
        d.setBilete(bilete.stream()
                .map(ClientMapper::toTicketRef)
                .collect(Collectors.toList()));

        return d;
    }

    private static TicketDTO toTicketDTO(TicketRef t) {
        TicketDTO dto = new TicketDTO();
        dto.setCod(t.getCod());
        dto.setTip(t.getTip());
        dto.setEventId(t.getEventId());
        dto.setPackageId(t.getPackageId());
        return dto;
    }

    private static TicketRef toTicketRef(TicketDTO dto) {
        TicketRef ref = new TicketRef();
        ref.setCod(dto.getCod());
        ref.setTip(dto.getTip());
        ref.setEventId(dto.getEventId());
        ref.setPackageId(dto.getPackageId());
        return ref;
    }


    public static PublicClientDTO toPublicDTO(ClientDocument d) {
        PublicClientDTO dto = new PublicClientDTO();
        dto.setId(d.getIdAsString());
        dto.setEmail(d.getEmail());
        dto.setNume(d.getNume());
        dto.setPrenume(d.getPrenume());

        List<TicketRef> bilete = d.getBilete() != null ? d.getBilete() : List.of();
        dto.setBilete(bilete.stream()
                .map(ClientMapper::toTicketDTO)
                .toList());

        return dto;
    }

}
