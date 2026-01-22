package com.example.clientservice.application.service;

import com.example.clientservice.domain.model.ClientDocument;
import com.example.clientservice.domain.repository.ClientRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class ClientService {

    private final ClientRepository repo;

    public ClientService(ClientRepository repo) {
        this.repo = repo;
    }

    public Optional<ClientDocument> findById(String id) {
        return repo.findById(new ObjectId(id));
    }

    public List<ClientDocument> findAlls(String nameLike) {
        if (!StringUtils.hasText(nameLike)) {
            return repo.findAll();
        }

        String regex = ".*" + nameLike + ".*";
        Set<ClientDocument> results = new LinkedHashSet<>();
        results.addAll(repo.findByNumeLike(regex));
        results.addAll(repo.findByPrenumeLike(regex));

        return new ArrayList<>(results);
    }

    public Optional<ClientDocument> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public ClientDocument updateByEmail(ClientDocument document) {
        if (document.getEmail() == null || document.getEmail().isBlank()) {
            throw new IllegalArgumentException("Campul 'email' este obligatoriu.");
        }

        return repo.findByEmail(document.getEmail())
                .map(existing -> {
                    existing.setNume(document.getNume());
                    existing.setPrenume(document.getPrenume());
                    existing.setPublic(document.isPublic());
                    existing.setSocial(document.getSocial());
                    return repo.save(existing);
                })
                .orElseGet(() -> repo.save(document));
    }

    public ClientDocument updateById(String id, ClientDocument document) {
        return repo.findById(new ObjectId(id))
                .map(existing -> {
                    existing.setEmail(document.getEmail());
                    existing.setNume(document.getNume());
                    existing.setPrenume(document.getPrenume());
                    existing.setPublic(document.isPublic());
                    existing.setSocial(document.getSocial());
                    return repo.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("Clientul nu exista."));
    }

    public void deleteById(String id) {
        repo.deleteById(new ObjectId(id));
    }

    public List<ClientDocument> findClientsByEventId(Integer eventId) {
        return repo.findAll().stream()
                .filter(client -> client.getBilete() != null)
                .filter(client -> client.getBilete().stream()
                        .anyMatch(b -> "event".equals(b.getTip()) &&
                                eventId.equals(b.getEventId())))
                .toList();
    }

    public List<ClientDocument> findClientsByPackageId(Integer packageId) {
        return repo.findAll().stream()
                .filter(client -> client.getBilete() != null)
                .filter(client -> client.getBilete().stream()
                        .anyMatch(b -> "package".equals(b.getTip()) &&
                                packageId.equals(b.getPackageId())))
                .toList();
    }

    public void updateClientEmail(String oldEmail, String newEmail) {
        Optional<ClientDocument> clientOpt = repo.findByEmail(oldEmail);
        if (clientOpt.isPresent()) {
            ClientDocument client = clientOpt.get();
            client.setEmail(newEmail);
            repo.save(client);
        }
    }
}