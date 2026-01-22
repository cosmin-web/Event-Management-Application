package com.example.idm.config;

import com.example.idm.db.UserRepository;
import com.example.idm.jwt.JwtUtil;
import com.example.idm.jwt.TokenBlackList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public UserRepository userRepository(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        return new UserRepository(url, username, password);
    }

    @Bean
    public JwtUtil jwtUtil(@Value("${jwt.secret:secret_default}") String secret) {
        return new JwtUtil(secret);
    }

    @Bean
    public TokenBlackList tokenBlackList() {
        return new TokenBlackList();
    }
}