package com.example.eventservice.application.service;

import com.example.eventservice.domain.model.EventEntity;
import com.example.eventservice.domain.model.PackageEntity;
import com.example.eventservice.domain.model.PackageEventEntity;
import com.example.eventservice.domain.model.TicketEntity;
import com.example.eventservice.domain.repository.EventRepository;
import com.example.eventservice.domain.repository.PackageEventRepository;
import com.example.eventservice.domain.repository.PackageRepository;
import com.example.eventservice.domain.repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PackageService packageService;

    @Autowired
    private PackageEventRepository packageEventRepository;


    public List<TicketEntity> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Optional<TicketEntity> getTicketByCode(String code) {
        return ticketRepository.findById(code);
    }

    public TicketEntity createTicketForEvent(Integer eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evenimentul nu exista."));

        long bileteVandute = ticketRepository.findByEveniment(event).size();

        if (event.getNumarLocuri() == null || bileteVandute >= event.getNumarLocuri()) {
            throw new IllegalStateException("Nu mai sunt locuri disponibile pentru acest eveniment.");
        }

        String cod = UUID.randomUUID().toString();
        TicketEntity ticket = new TicketEntity();
        ticket.setCod(cod);
        ticket.setEveniment(event);
        ticket.setPachet(null);

        return ticketRepository.save(ticket);
    }

    @Transactional
    public TicketEntity createTicketForPackage(Integer packageId) {
        PackageEntity pachet = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Pachetul nu exista."));

        int available = packageService.calculeazaLocuriDisponibile(pachet);
        if (available <= 0) {
            throw new IllegalStateException("Nu mai sunt locuri disponibile pentru acest pachet.");
        }

        String cod = UUID.randomUUID().toString();
        TicketEntity ticket = new TicketEntity();
        ticket.setCod(cod);
        ticket.setPachet(pachet);
        ticket.setEveniment(null);

        return ticketRepository.save(ticket);
    }


    public void deleteTicket(String cod) {
        if (!ticketRepository.existsById(cod)) {
            throw new IllegalArgumentException("Biletul nu exista.");
        }
        ticketRepository.deleteById(cod);
    }

    public List<TicketEntity> getTicketsByEvent(EventEntity event) {
        return ticketRepository.findByEveniment(event);
    }

    public List<TicketEntity> getTicketsByPackage(PackageEntity pachet) {
        return ticketRepository.findByPachet(pachet);
    }

    public Page<TicketEntity> searchTickets(String eventName, String packageName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<TicketEntity> allTickets = ticketRepository.findAll();

        List<TicketEntity> filtered = allTickets.stream()
                .filter(t -> eventName == null || (t.getEveniment() != null && t.getEveniment().getNume().toLowerCase().contains(eventName.toLowerCase())))
                .filter(t -> packageName == null || (t.getPachet() != null && t.getPachet().getNume().toLowerCase().contains(packageName.toLowerCase())))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<TicketEntity> paginated = start >= filtered.size() ? List.of() : filtered.subList(start, end);

        return new PageImpl<>(paginated, pageable, filtered.size());
    }
}
