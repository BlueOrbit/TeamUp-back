package com.blueorbit.teamup.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class SessionTokenService {
    private final ConcurrentMap<String, TokenSession> tokenStore = new ConcurrentHashMap<>();
    private final Duration tokenTtl;

    public SessionTokenService(@Value("${security.token.expire-hours:24}") long expireHours) {
        this.tokenTtl = Duration.ofHours(Math.max(1, expireHours));
    }

    public String issueToken(Long uid) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, new TokenSession(uid, Instant.now().plus(tokenTtl)));
        return token;
    }

    public Long verifyAndGetUid(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        TokenSession session = tokenStore.get(token);
        if (session == null) {
            return null;
        }
        if (Instant.now().isAfter(session.expireAt())) {
            tokenStore.remove(token);
            return null;
        }
        return session.uid();
    }

    public void revoke(String token) {
        if (token != null && !token.isEmpty()) {
            tokenStore.remove(token);
        }
    }

    private record TokenSession(Long uid, Instant expireAt) {
    }
}
