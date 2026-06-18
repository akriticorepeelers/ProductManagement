package com.ecommerce.authservice.serviceimpl;

import com.ecommerce.authservice.dto.JwtResponse;
import com.ecommerce.authservice.dto.TokenRefreshRequest;
import com.ecommerce.authservice.dto.UserLoginRequest;
import com.ecommerce.authservice.dto.UserRegisterRequest;
import com.ecommerce.authservice.entity.User;
import com.ecommerce.authservice.repository.UserRepository;
import com.ecommerce.authservice.service.AuthService;
import com.ecommerce.authservice.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthServiceImpl orchestrates authentication flows.
 * Handles local user reference persistence and delegates user/token lifecycle management to KeycloakService.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KeycloakService keycloakService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public User registerUser(UserRegisterRequest request) {
        // Validate local duplicates
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }

        // 1. Register in Keycloak first
        String keycloakId = keycloakService.registerUser(request);

        // 2. Sync user profile info locally to the reference MySQL table
        User user = User.builder()
                .keycloakId(keycloakId)
                .username(request.getUsername())
                .email(request.getEmail())
                .role(request.getRole().toUpperCase())
                .build();

        return userRepository.save(user);
    }

    @Override
    public JwtResponse loginUser(UserLoginRequest request) {
        return keycloakService.login(request);
    }

    @Override
    public JwtResponse refreshUserToken(TokenRefreshRequest request) {
        return keycloakService.refresh(request);
    }
}
