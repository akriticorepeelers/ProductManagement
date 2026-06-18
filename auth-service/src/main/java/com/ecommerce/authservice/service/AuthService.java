package com.ecommerce.authservice.service;

import com.ecommerce.authservice.dto.JwtResponse;
import com.ecommerce.authservice.dto.TokenRefreshRequest;
import com.ecommerce.authservice.dto.UserLoginRequest;
import com.ecommerce.authservice.dto.UserRegisterRequest;
import com.ecommerce.authservice.entity.User;

/**
 * AuthService declares business logic methods for registration, login, and token refresh.
 */
public interface AuthService {

    /**
     * Registers a new user inside Keycloak and syncs details to the local MySQL table.
     */
    User registerUser(UserRegisterRequest request);

    /**
     * Authenticates user against Keycloak and returns token credentials payload.
     */
    JwtResponse loginUser(UserLoginRequest request);

    /**
     * Refreshes the user access token.
     */
    JwtResponse refreshUserToken(TokenRefreshRequest request);
}
