package com.example.clientservice.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${client.event-service.base-url}")
    private String baseUrl;

    @Bean
    WebClient eventWebClient(@Value("${client.event-service.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @PostConstruct
    void logBaseUrl() {
        log.info("Event Service base URL = {}", baseUrl);
    }
}
