package com.example.idm.grpc;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.idm.db.User;
import com.example.idm.db.UserRepository;
import com.example.idm.jwt.JwtUtil;
import com.example.idm.jwt.TokenBlackList;
import io.grpc.stub.StreamObserver;

import com.example.idm.grpc.*;
import org.springframework.stereotype.Service;

@Service
public class IdmServiceImpl extends IdmServiceGrpc.IdmServiceImplBase {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final TokenBlackList tokenBlackList;

    public IdmServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, TokenBlackList tokenBlackList) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.tokenBlackList = tokenBlackList;
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        String username = request.getUsername();
        String password = request.getPassword();

        User user = userRepository.findByEmailAndPassword(username, password);

        LoginResponse.Builder builder = LoginResponse.newBuilder();

        if (user == null) {
            builder.setSuccess(false)
                    .setMessage("Invalid credentials");
        } else {
            String token = jwtUtil.generateToken(
                    user.getId(),
                    user.getRole(),
                    user.getEmail()
            );
            builder.setSuccess(true)
                    .setToken(token)
                    .setMessage("OK");
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }


    @Override
    public void validateToken(TokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
        String token = request.getToken();
        ValidateTokenResponse.Builder builder = ValidateTokenResponse.newBuilder();

        if (tokenBlackList.isBlacklisted(token)) {
            builder.setValid(false)
                    .setMessage("Token is blacklisted");
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        try {
            DecodedJWT jwt = jwtUtil.validate(token);
            String sub = jwt.getSubject();
            String role = jwt.getClaim("role").asString();
            String email = jwt.getClaim("email").asString();

            builder.setValid(true)
                    .setSub(sub)
                    .setRole(role)
                    .setEmail(email != null ? email : "")
                    .setMessage("OK");
        } catch (JWTVerificationException ex) {
            tokenBlackList.blacklist(token);
            builder.setValid(false)
                    .setMessage("Token invalid or expired");
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void logout(TokenRequest request, StreamObserver<LogoutResponse> responseObserver) {
        String token = request.getToken();
        tokenBlackList.blacklist(token);

        LogoutResponse response = LogoutResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Token invalidated")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}