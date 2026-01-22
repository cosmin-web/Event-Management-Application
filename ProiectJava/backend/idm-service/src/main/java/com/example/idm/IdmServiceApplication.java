package com.example.idm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class IdmServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdmServiceApplication.class, args);
    }
}