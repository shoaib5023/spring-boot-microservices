package com.example.demo.security;
// package com.example.demo.Security;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.Customizer;
// import
// org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import
// org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.web.SecurityFilterChain;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {
// @Bean
// public SecurityFilterChain basicAuth(HttpSecurity http) throws Exception {
// http
// .csrf(csrf -> csrf.disable())
// .authorizeHttpRequests(
// auth -> auth.requestMatchers("/api/auth/register",
// "/api/auth/login").permitAll().anyRequest().authenticated())
// .httpBasic(Customizer.withDefaults());
// return http.build();

// }

// }
