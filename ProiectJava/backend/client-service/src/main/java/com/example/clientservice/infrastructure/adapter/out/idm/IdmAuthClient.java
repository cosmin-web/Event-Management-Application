package com.example.clientservice.infrastructure.adapter.out.idm;

import com.example.idm.grpc.IdmServiceGrpc;
import com.example.idm.grpc.LoginRequest;
import com.example.idm.grpc.LoginResponse;
import com.example.idm.grpc.TokenRequest;
import com.example.idm.grpc.ValidateTokenResponse;
import com.example.idm.grpc.LogoutResponse;
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
