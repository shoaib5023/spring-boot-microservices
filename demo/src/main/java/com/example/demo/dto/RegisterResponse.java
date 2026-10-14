package com.example.demo.dto;

public class RegisterResponse {

    private String message;
    private String username;

    public RegisterResponse(String message, String username) {
        this.message = message;
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

}
