package com.example.clientservice.infrastructure.config;

import com.example.idm.grpc.IdmServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdmGrpcConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel idmChannel(
            @Value("${idm.host:idm-service}") String host,
            @Value("${idm.port:9090}") int port
    ) {
        return ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    public IdmServiceGrpc.IdmServiceBlockingStub idmBlockingStub(ManagedChannel idmChannel) {
        return IdmServiceGrpc.newBlockingStub(idmChannel);
    }
}
