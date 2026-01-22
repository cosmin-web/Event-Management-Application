package com.example.clientservice.infrastructure.config;

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
    public OpenAPI clientServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Client Service API")
                        .description("Documentatia API-ului pentru gestionarea clientilor si biletelor.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Client Service")
                                .email("contact@clientapp.local"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentatie completa")
                        .url("https://www.baeldung.com/spring-rest-openapi-documentation"));
    }
}
