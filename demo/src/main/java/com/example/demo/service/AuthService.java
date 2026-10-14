
package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.RegisterResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final RefreshTokenService refreshTokenService;

        public AuthService(
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        RefreshTokenService refreshTokenService) {
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtService = jwtService;
                this.refreshTokenService = refreshTokenService;
        }

        @Transactional
        public RegisterResponse register(RegisterRequest request) {

                if (request.getUsername() == null
                                || request.getUsername().isBlank()) {
                        throw new IllegalArgumentException("Username is required");
                }

                if (request.getEmail() == null
                                || request.getEmail().isBlank()) {
                        throw new IllegalArgumentException("Email is required");
                }

                if (request.getPassword() == null
                                || request.getPassword().isBlank()) {
                        throw new IllegalArgumentException("Password is required");
                }

                if (userRepository.existsByUsername(request.getUsername())) {
                        throw new IllegalArgumentException("Username already exists");
                }

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new IllegalArgumentException("Email already exists");
                }

                if (userRepository.existsByUsername(request.getUsername()))

                {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "User already exists");
                }

                User user = new User();
                user.setUsername(request.getUsername());
                user.setEmail(request.getEmail());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setFirstName(request.getFirstName());
                user.setLastName(request.getLastName());

                User savedUser = userRepository.save(user);

                return new RegisterResponse(
                                "User registered successfully",
                                savedUser.getUsername());
        }

        // @Transactional(readOnly = true)
        public AuthResponse login(LoginRequest request) {

                if (request.getUsername() == null
                                || request.getUsername().isBlank()
                                || request.getPassword() == null
                                || request.getPassword().isBlank()) {
                        throw new IllegalArgumentException(
                                        "Username and password are required");
                }

                User user = userRepository
                                .findByUsername(request.getUsername())
                                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

                if (!passwordEncoder.matches(
                                request.getPassword(), user.getPassword())) {
                        throw new RuntimeException("Invalid username or password");
                }

                String accessToken = jwtService.generateAccessToken(user);
                String refreshToken = refreshTokenService.createRefreshToken(user);

                System.out.println("Generated access token is null: "
                                + (accessToken == null));

                System.out.println("Generated token length: "
                                + (accessToken == null ? 0 : accessToken.length()));

                return new AuthResponse(
                                accessToken,
                                jwtService.getAccessTokenExpiration() / 1000,
                                "Login successful",
                                refreshToken,
                                "Bearer",
                                user.getUsername());
        }

        @Transactional
        public AuthResponse refresh(RefreshTokenRequest request) {

                var oldSession = refreshTokenService.consumeRefreshToken(
                                request.getRefreshToken());

                User user = userRepository.findById(oldSession.getUserId())
                                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                                                org.springframework.http.HttpStatus.UNAUTHORIZED,
                                                "User no longer exists"));

                String newAccessToken = jwtService.generateAccessToken(user);

                // Rotate: issue a new refresh token and invalidate the old one.
                String newRefreshToken = refreshTokenService.createRefreshToken(user);

                return new AuthResponse(
                                newAccessToken,
                                jwtService.getAccessTokenExpiration() / 1000,
                                "Token refreshed successfully",
                                newRefreshToken,
                                "Bearer",
                                user.getUsername());
        }

        @Transactional
        public void logout(RefreshTokenRequest request) {
                refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        }
}