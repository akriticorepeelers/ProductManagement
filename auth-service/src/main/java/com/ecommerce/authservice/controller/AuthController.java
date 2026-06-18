package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.dto.JwtResponse;
import com.ecommerce.authservice.dto.TokenRefreshRequest;
import com.ecommerce.authservice.dto.UserLoginRequest;
import com.ecommerce.authservice.dto.UserRegisterRequest;
import com.ecommerce.authservice.entity.User;
import com.ecommerce.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController exposes public authentication APIs.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a user in the system.
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody UserRegisterRequest request) {
        User user = authService.registerUser(request);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    /**
     * Authenticates user and returns JWT access/refresh credentials.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody UserLoginRequest request) {
        JwtResponse response = authService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes expired access tokens using a valid refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        JwtResponse response = authService.refreshUserToken(request);
        return ResponseEntity.ok(response);
    }
}
