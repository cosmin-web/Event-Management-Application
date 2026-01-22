package com.example.clientservice.infrastructure.config;

import com.example.clientservice.domain.model.ClientDocument;
import com.example.clientservice.domain.repository.ClientRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ClientDataInitializer implements ApplicationRunner {

    private final ClientRepository repo;

    public ClientDataInitializer(ClientRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(ApplicationArguments args) {

        createIfMissing("admin@local", "Admin", "System");
        createIfMissing("owner@local", "Owner", "Event");
        createIfMissing("client@local", "Client", "Test");

        System.out.println("ClientDataInitializer: sincronizare cu sql finalizata.");
    }

    private void createIfMissing(String email, String nume, String prenume) {
        if (repo.findByEmail(email).isEmpty()) {
            ClientDocument c = new ClientDocument();
            c.setEmail(email);
            c.setNume(nume);
            c.setPrenume(prenume);
            repo.save(c);
            System.out.println("Adaugat client in Mongo: " + email);
        }
    }
}

