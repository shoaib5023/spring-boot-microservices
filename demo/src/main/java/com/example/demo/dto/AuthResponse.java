
package com.example.demo.dto;

public class AuthResponse {

    private String accessToken;
    private long expiresIn;
    private String message;
    private String refreshToken;
    private String tokenType;
    private String username;

    // Registration response
    public AuthResponse(String message, String username) {
        this.message = message;
        this.username = username;
    }

    // Login response
    public AuthResponse(
            String accessToken,
            long expiresIn,
            String message,
            String refreshToken,
            String tokenType,
            String username) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.message = message;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.username = username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getMessage() {
        return message;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getUsername() {
        return username;
    }
}