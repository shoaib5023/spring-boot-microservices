
package com.example.demo.service;

import com.example.demo.entity.RefreshSession;
import com.example.demo.entity.User;
import com.example.demo.repository.RefreshSessionRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final RefreshSessionRepository sessionRepository;
    private final long refreshTokenExpiration;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshSessionRepository sessionRepository,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {

        this.sessionRepository = sessionRepository;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @Transactional
    public String createRefreshToken(User user) {

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        RefreshSession session = new RefreshSession();
        session.setUserId(user.getId());
        session.setTokenHash(hashToken(rawToken));
        session.setExpiresAt(
                Instant.now().plusMillis(refreshTokenExpiration));

        sessionRepository.save(session);

        // Return the raw token only to the client.
        return rawToken;
    }

    @Transactional
    public RefreshSession consumeRefreshToken(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw invalidToken();
        }

        RefreshSession session = sessionRepository
                .findByTokenHash(hashToken(rawToken))
                .orElseThrow(this::invalidToken);

        if (session.isRevoked()
                || !session.getExpiresAt().isAfter(Instant.now())) {
            throw invalidToken();
        }

        // One-time use: the old refresh token cannot be reused.
        session.setRevoked(true);
        sessionRepository.save(session);

        return session;
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        sessionRepository.findByTokenHash(hashToken(rawToken))
                .ifPresent(session -> {
                    session.setRevoked(true);
                    sessionRepository.save(session);
                });
    }

    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 is not available", ex);
        }
    }

    private ResponseStatusException invalidToken() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid, expired or revoked refresh token");
    }
}