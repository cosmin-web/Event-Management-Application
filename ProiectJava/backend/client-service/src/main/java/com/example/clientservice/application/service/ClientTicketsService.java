package com.example.clientservice.application.service;

import com.example.clientservice.infrastructure.adapter.out.event.EventApiClient;
import com.example.clientservice.infrastructure.adapter.out.event.dto.TicketData;
import com.example.clientservice.domain.model.ClientDocument;
import com.example.clientservice.domain.model.TicketRef;
import com.example.clientservice.domain.repository.ClientRepository;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClientTicketsService {

    private final ClientRepository repo;
    private final EventApiClient eventApiClient;

    public ClientTicketsService(ClientRepository repo, EventApiClient eventApiClient) {
        this.repo = repo;
        this.eventApiClient = eventApiClient;
    }

    public Optional<TicketData> validateTicket(
            String email,
            String cod,
            boolean saveIfValid,
            String authorizationHeader) {

        TicketData data = eventApiClient.validateAndFetchTicket(cod, authorizationHeader);

        if (data == null) {
            throw new RuntimeException("Biletul nu a fost gasit.");
        }

        TicketRef ref = new TicketRef();
        ref.setCod(cod);

        if (data.getEvent() != null) {
            ref.setTip("event");
            ref.setEventId(data.getEvent().getId());
        } else if (data.get_package() != null) {
            ref.setTip("package");
            ref.setPackageId(data.get_package().getId());
        } else {
            throw new RuntimeException("Bilet invalid.");
        }

        if (saveIfValid) {
            ClientDocument client = repo.findByEmail(email).orElse(null);

            if (client == null) {
                client = new ClientDocument();
                client.setEmail(email);
                client.setBilete(new ArrayList<>());
            }

            client.getBilete().add(ref);
            repo.save(client);
        }

        return Optional.of(data);
    }

    public List<TicketData> listDetailedTickets(
            String email,
            String authorizationHeader) {

        ClientDocument client = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Clientul nu a fost gasit."));

        List<TicketData> result = new ArrayList<>();

        for (TicketRef ref : client.getBilete()) {
            TicketData data = eventApiClient.validateAndFetchTicket(ref.getCod(), authorizationHeader);
            if (data != null) {
                result.add(data);
            }
        }

        return result;
    }

    public TicketData buyTicketForEvent(String email, Integer eventId, String serviceToken) {

        TicketData data = eventApiClient.createTicketForEvent(eventId, serviceToken);

        if(data == null) {
            throw new RuntimeException("Nu s-a putut crea biletul pentru eveniment.");
        }

        TicketRef ref = new TicketRef();
        ref.setCod(data.getCod());
        ref.setTip("event");
        ref.setEventId(eventId);

        ClientDocument client = repo.findByEmail(email).orElse(null);

        if(client == null) {
            client = new ClientDocument();
            client.setEmail(email);
            client.setBilete(new ArrayList<>());
        }

        client.getBilete().add(ref);
        repo.save(client);

        return data;
    }

    public TicketData buyTicketForPackage(String email, Integer packageId, String serviceToken) {

        TicketData data = eventApiClient.createTicketForPackage(packageId, serviceToken);

        if(data == null) {
            throw new RuntimeException("Nu s-a putut crea biletul pentru pachet.");
        }

        TicketRef ref = new TicketRef();
        ref.setCod(data.getCod());
        ref.setTip("package");
        ref.setPackageId(packageId);

        ClientDocument client = repo.findByEmail(email).orElse(null);

        if(client == null) {
            client = new ClientDocument();
            client.setEmail(email);
            client.setBilete(new ArrayList<>());
        }

        client.getBilete().add(ref);
        repo.save(client);

        return data;
    }

    public void deleteTicketEverywhere(String cod, String authorizationHeader) {
        eventApiClient.deleteTicket(cod, authorizationHeader);

        List<ClientDocument> allClients = repo.findAll();

        for (ClientDocument client : allClients) {
            if (client.getBilete() == null || client.getBilete().isEmpty()) {
                continue;
            }

            List<TicketRef> filtered = client.getBilete().stream()
                    .filter(t -> !cod.equals(t.getCod()))
                    .toList();

            if (filtered.size() != client.getBilete().size()) {
                client.setBilete(filtered);
                repo.save(client);
            }
        }
    }

}
