package com.example.idm.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GrpcServerRunner implements CommandLineRunner {

    private final IdmServiceImpl idmService;

    @Value("${grpc.server.port:9090}")
    private int port;

    public GrpcServerRunner(IdmServiceImpl idmService) {
        this.idmService = idmService;
    }

    @Override
    public void run(String... args) {
        new Thread(() -> {
            try {
                System.out.println("Starting gRPC Server on port " + port);
                Server server = ServerBuilder.forPort(port)
                        .addService(idmService)
                        .build();
                server.start();
                server.awaitTermination();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}