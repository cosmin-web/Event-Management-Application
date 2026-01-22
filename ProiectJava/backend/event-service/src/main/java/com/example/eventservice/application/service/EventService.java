package com.example.eventservice.application.service;

import com.example.eventservice.domain.model.EventEntity;
import com.example.eventservice.domain.model.PackageEventEntity;
import com.example.eventservice.domain.repository.EventRepository;
import com.example.eventservice.domain.repository.PackageEventRepository;
import com.example.eventservice.domain.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PackageEventRepository packageEventRepository;

    @Autowired
    private TicketRepository ticketRepository;


    public List<EventEntity> getAllEvents() {
        return eventRepository.findAll();
    }

    public Optional<EventEntity> getEventById(Integer id) {
        return eventRepository.findById(id);
    }

    public EventEntity createEvent(EventEntity event) {
        if(event.getNumarLocuri() == null || event.getNumarLocuri() <= 0) {
            throw new IllegalArgumentException("Numarul de locuri trebuie sa fie mai mare decat 0.");
        }
        return eventRepository.save(event);
    }

    public EventEntity updateEvent(Integer id, EventEntity updatedEvent) {
        return eventRepository.findById(id)
                .map(event -> {

                    int bileteEveniment = ticketRepository.findByEveniment(event).size();
                    int biletePachete = countPackageTicketsImpactForEvent(event);
                    int totalVandute = bileteEveniment + biletePachete;

                    if(totalVandute > 0) {
                        if(!event.getNume().equals(updatedEvent.getNume()) ||
                                !event.getLocatie().equals(updatedEvent.getLocatie()) ||
                                !event.getNumarLocuri().equals(updatedEvent.getNumarLocuri())) {

                            throw new IllegalStateException(
                                    "Nu poti modifica numele, locatia sau numarul de locuri daca exista deja bilete vandute!"
                            );
                        }
                    }

                    event.setNume(updatedEvent.getNume());
                    event.setLocatie(updatedEvent.getLocatie());
                    event.setDescriere(updatedEvent.getDescriere());
                    event.setNumarLocuri((updatedEvent.getNumarLocuri()));
                    return eventRepository.save(event);
                }).orElseThrow(() -> new IllegalArgumentException("Evenimentul nu exista."));
    }

    public void deleteEvent(Integer id) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evenimentul nu exista."));

        packageEventRepository.deleteByEveniment(event);
        eventRepository.delete(event);
    }

    public List<PackageEventEntity> getPackagesForEvent(EventEntity event) {
        return packageEventRepository.findByEveniment(event);
    }

    public Page<EventEntity> searchEvents(String name, String location, Integer availableTickets, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<EventEntity> allEvents = eventRepository.findAll(); //

        List<EventEntity> filtered = allEvents.stream()
                .filter(e -> name == null || e.getNume().toLowerCase().contains(name.toLowerCase()))
                .filter(e -> location == null || (e.getLocatie() != null && e.getLocatie().toLowerCase().contains(location.toLowerCase())))
                .filter(e -> {
                    if (availableTickets == null) return true;

                    int totalCapacity = e.getNumarLocuri() != null ? e.getNumarLocuri() : 0;
                    int soldDirect = countTicketsSold(e);
                    int soldViaPackage = countPackageTicketsImpactForEvent(e);

                    int realAvailable = totalCapacity - soldDirect - soldViaPackage;

                    return realAvailable >= availableTickets;
                })
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<EventEntity> paginated = start >= filtered.size() ?
                List.of() : filtered.subList(start, end);

        return new PageImpl<>(paginated, pageable, filtered.size());
    }

    public int countTicketsSold(EventEntity event) {
        return ticketRepository.findByEveniment(event).size();
    }

    public int countPackageTicketsImpactForEvent(EventEntity event) {
        var rels = packageEventRepository.findByEveniment(event);

        return rels.stream()
                .map(pe -> pe.getPachet())
                .distinct()
                .mapToInt(p -> ticketRepository.findByPachet(p).size())
                .sum();
    }
}
