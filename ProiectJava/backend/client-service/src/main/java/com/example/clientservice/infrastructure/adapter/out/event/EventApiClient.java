package com.example.clientservice.infrastructure.adapter.out.event;

import com.example.clientservice.infrastructure.adapter.out.event.dto.TicketData;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class EventApiClient {
    private final WebClient eventWebClient;

    public EventApiClient(WebClient eventWebClient) {
        this.eventWebClient = eventWebClient;
    }

    public TicketData validateAndFetchTicket(String cod, String authorizationHeader) {
        return eventWebClient.get()
                .uri(uri -> uri.path("/tickets/{cod}").build(cod))
                .header("Authorization", authorizationHeader)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> Mono.error(new RuntimeException("Biletul nu a fost gasit: " + cod)))
                .onStatus(status -> status.is5xxServerError(),
                        response -> Mono.error(new RuntimeException("Server eroare")))
                .bodyToMono(TicketData.class)
                .onErrorResume(e -> Mono.empty())
                .block();
    }

    public TicketData createTicketForEvent(Integer eventId, String serviceToken) {
        return eventWebClient.put()
                .uri(uri -> uri.path("/events/{eventId}/tickets").build(eventId))
                .header("Authorization", "Bearer " + serviceToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> Mono.error(new RuntimeException("Nu pot crea bilet pentru eveniment " + eventId)))
                .onStatus(status -> status.is5xxServerError(),
                        response -> Mono.error(new RuntimeException("Server eroare la event-service")))
                .bodyToMono(TicketData.class)
                .block();
    }

    public TicketData createTicketForPackage(Integer packageId, String serviceToken) {
        return eventWebClient.put()
                .uri(uri -> uri.path("/event-packets/{packageId}/tickets").build(packageId))
                .header("Authorization", "Bearer " + serviceToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> Mono.error(new RuntimeException("Nu pot crea bilet pentru pachet " + packageId)))
                .onStatus(status -> status.is5xxServerError(),
                        response -> Mono.error(new RuntimeException("Server eroare la event-service")))
                .bodyToMono(TicketData.class)
                .block();
    }

    public void deleteTicket(String cod, String authorizationHeader) {
        eventWebClient.delete()
                .uri(uri -> uri.path("/tickets/{cod}").build(cod))
                .header("Authorization", authorizationHeader)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}