package com.example.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.example.demo.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
                implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(
                        String email)
                        throws UsernameNotFoundException {

                var user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found"));

                var authorities = user.getRoles()
                                .stream()
                                .map(role -> new SimpleGrantedAuthority(
                                                role.getName()))
                                .toList();

                return new org.springframework.security.core.userdetails.User(
                                user.getEmail(),
                                user.getPassword(),
                                user.isEnabled(),
                                true,
                                true,
                                true,
                                authorities);
        }
}