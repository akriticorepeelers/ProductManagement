package com.ecommerce.authservice.service;

import com.ecommerce.authservice.dto.JwtResponse;
import com.ecommerce.authservice.dto.TokenRefreshRequest;
import com.ecommerce.authservice.dto.UserLoginRequest;
import com.ecommerce.authservice.dto.UserRegisterRequest;

/**
 * KeycloakService declares methods to interface with the Keycloak server API.
 */
public interface KeycloakService {

    /**
     * Registers user in Keycloak and returns Keycloak's unique user UUID.
     */
    String registerUser(UserRegisterRequest request);

    /**
     * Authenticates username/password credentials with Keycloak.
     */
    JwtResponse login(UserLoginRequest request);

    /**
     * Obtains a refreshed token set using an active refresh token.
     */
    JwtResponse refresh(TokenRefreshRequest request);
}
