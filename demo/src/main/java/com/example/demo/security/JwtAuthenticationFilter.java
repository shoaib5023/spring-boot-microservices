
package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtService jwtService;
        private final UserRepository userRepository;

        public JwtAuthenticationFilter(
                        JwtService jwtService,
                        UserRepository userRepository) {
                this.jwtService = jwtService;
                this.userRepository = userRepository;
        }

        @Override
        protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain)
                        throws ServletException, IOException {

                String authHeader = request.getHeader("Authorization");

                // Allow requests without a token to continue.
                // SecurityConfig decides whether authentication is required.
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        filterChain.doFilter(request, response);
                        return;
                }

                String token = authHeader.substring(7);

                try {
                        if (jwtService.isTokenValid(token)
                                        && SecurityContextHolder.getContext()
                                                        .getAuthentication() == null) {

                                // Your JwtService uses email as the JWT subject.
                                String email = jwtService.extractUsername(token);

                                User user = userRepository.findByEmail(email)
                                                .orElse(null);

                                if (user != null && user.isEnabled()) {
                                        var authorities = user.getRoles().stream()
                                                        .map(role -> new SimpleGrantedAuthority(
                                                                        role.getName()))
                                                        .collect(Collectors.toList());

                                        var authentication = new UsernamePasswordAuthenticationToken(
                                                        user.getEmail(),
                                                        null,
                                                        authorities);

                                        SecurityContextHolder.getContext()
                                                        .setAuthentication(authentication);
                                }
                        }
                } catch (io.jsonwebtoken.JwtException
                                | IllegalArgumentException ex) {
                        // Invalid or expired token: don't authenticate the request.
                        SecurityContextHolder.clearContext();
                }

                filterChain.doFilter(request, response);
        }
}