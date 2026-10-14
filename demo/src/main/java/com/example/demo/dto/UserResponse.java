package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class UserResponse {

    private Long id;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private Set<String> roles;
}