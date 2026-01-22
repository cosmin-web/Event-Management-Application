package com.example.eventservice.infrastructure.adapter.out.idm;

import com.example.idm.grpc.*;
import org.springframework.stereotype.Component;

@Component
public class IdmAuthClient {

    private final IdmServiceGrpc.IdmServiceBlockingStub stub;

    public IdmAuthClient(IdmServiceGrpc.IdmServiceBlockingStub stub) {
        this.stub = stub;
    }

    public LoginResponse login(String username, String password) {
        LoginRequest request = LoginRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();

        return stub.login(request);
    }

    public ValidateTokenResponse validate(String token) {
        TokenRequest request = TokenRequest.newBuilder()
                .setToken(token)
                .build();

        return stub.validateToken(request);
    }

    public LogoutResponse logout(String token) {
        TokenRequest request = TokenRequest.newBuilder()
                .setToken(token)
                .build();

        return stub.logout(request);
    }
}
