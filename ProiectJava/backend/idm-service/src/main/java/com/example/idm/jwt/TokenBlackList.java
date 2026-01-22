package com.example.idm.jwt;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBlackList {

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public void blacklist(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
