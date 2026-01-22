package com.example.eventservice.infrastructure.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI eventManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Event Management API")
                        .description("Documentatia API-ului pentru aplicatia de gestionare a evenimentelor si pachetelor.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Event Management")
                                .email("contact@eventapp.local"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Ghid complet al proiectului")
                        .url("https://www.baeldung.com/spring-rest-openapi-documentation"));
    }
}
