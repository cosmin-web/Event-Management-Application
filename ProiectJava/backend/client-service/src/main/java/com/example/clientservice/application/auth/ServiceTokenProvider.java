package com.example.clientservice.application.auth;

import com.example.clientservice.infrastructure.adapter.out.idm.IdmAuthClient;
import com.example.idm.grpc.LoginRequest;
import com.example.idm.grpc.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceTokenProvider {

    private final IdmAuthClient idmAuthClient;
    private final String serviceUsername;
    private final String servicePassword;

    public ServiceTokenProvider(
            IdmAuthClient idmAuthClient,
            @Value("${idm.service.username}") String serviceUsername,
            @Value("${idm.service.password}") String servicePassword) {

        this.idmAuthClient = idmAuthClient;
        this.serviceUsername = serviceUsername;
        this.servicePassword = servicePassword;
    }

    public String getServiceToken() {
        LoginResponse resp = idmAuthClient.login(serviceUsername, servicePassword);

        if(!resp.getSuccess()) {
            throw new IllegalStateException("Nu ma pot loga cu userul de serviciu: " + resp.getMessage());
        }

        return resp.getToken();
    }
}
