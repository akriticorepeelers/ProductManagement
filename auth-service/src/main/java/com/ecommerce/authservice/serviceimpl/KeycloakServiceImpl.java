package com.ecommerce.authservice.serviceimpl;

import com.ecommerce.authservice.dto.JwtResponse;
import com.ecommerce.authservice.dto.TokenRefreshRequest;
import com.ecommerce.authservice.dto.UserLoginRequest;
import com.ecommerce.authservice.dto.UserRegisterRequest;
import com.ecommerce.authservice.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * KeycloakServiceImpl interacts with the Keycloak Server REST API using Spring's RestClient.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${keycloak.admin.realm}")
    private String adminRealm;

    private final RestClient restClient = RestClient.builder().build();

    /**
     * Obtains an Admin Access Token from Keycloak using master realm admin credentials.
     */
    private String getAdminAccessToken() {
        String tokenUrl = authServerUrl + "/realms/" + adminRealm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", adminClientId);
        body.add("username", adminUsername);
        body.add("password", adminPassword);

        try {
            Map<?, ?> response = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("access_token")) {
                return (String) response.get("access_token");
            }
        } catch (Exception e) {
            log.error("Failed to retrieve Keycloak admin access token: {}", e.getMessage());
            throw new RuntimeException("Keycloak connection error: " + e.getMessage());
        }
        throw new RuntimeException("Could not obtain Keycloak admin token.");
    }

    @Override
    public String registerUser(UserRegisterRequest request) {
        String adminToken = getAdminAccessToken();
        String registerUrl = authServerUrl + "/admin/realms/" + realm + "/users";

        // Construct User Representation payload
        Map<String, Object> credential = Map.of(
                "type", "password",
                "value", request.getPassword(),
                "temporary", false
        );

        Map<String, Object> userPayload = Map.of(
                "username", request.getUsername(),
                "email", request.getEmail(),
                "firstName", request.getUsername(),
                "lastName", request.getUsername(),
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(credential)
        );

        // 1. Create User in Keycloak
        ResponseEntity<Void> response;
        try {
            response = restClient.post()
                    .uri(registerUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(userPayload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Error creating user in Keycloak: {}", e.getMessage());
            throw new RuntimeException("Keycloak registration error: " + e.getMessage());
        }

        if (response.getStatusCode().value() != 201) {
            throw new RuntimeException("Keycloak registration failed. Status: " + response.getStatusCode());
        }

        // 2. Extract Keycloak User ID (UUID) from the Location header
        List<String> locationHeader = response.getHeaders().get(HttpHeaders.LOCATION);
        if (locationHeader == null || locationHeader.isEmpty()) {
            throw new RuntimeException("Missing Location header in Keycloak user registration response.");
        }

        String userLocation = locationHeader.get(0);
        String keycloakUserId = userLocation.substring(userLocation.lastIndexOf("/") + 1);
        log.info("Successfully created Keycloak user ID: {}", keycloakUserId);

        // 3. Assign Role to user
        assignRoleToUser(adminToken, keycloakUserId, request.getRole());

        return keycloakUserId;
    }

    /**
     * Wires the specified realm role to the new user.
     */
    private void assignRoleToUser(String adminToken, String keycloakUserId, String roleName) {
        // Fetch Realm Role representation first
        String roleUrl = authServerUrl + "/admin/realms/" + realm + "/roles/" + roleName;
        Map<?, ?> roleRep;
        try {
            roleRep = restClient.get()
                    .uri(roleUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("Failed to fetch role info for '{}' from Keycloak: {}", roleName, e.getMessage());
            throw new RuntimeException("Keycloak role mapping error: " + e.getMessage());
        }

        if (roleRep == null) {
            throw new RuntimeException("Role '" + roleName + "' does not exist in Keycloak realm.");
        }

        // Post Role mapping configuration
        String mappingUrl = authServerUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/role-mappings/realm";
        try {
            restClient.post()
                    .uri(mappingUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(roleRep))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Assigned role '{}' to Keycloak User UUID: {}", roleName, keycloakUserId);
        } catch (Exception e) {
            log.error("Failed to map role '{}' to user '{}': {}", roleName, keycloakUserId, e.getMessage());
            throw new RuntimeException("Keycloak role assignment mapping failed.");
        }
    }

    @Override
    public JwtResponse login(UserLoginRequest request) {
        String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", request.getUsername());
        body.add("password", request.getPassword());

        try {
            return restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(JwtResponse.class);
        } catch (Exception e) {
            log.error("Keycloak authentication login failed: {}", e.getMessage());
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Override
    public JwtResponse refresh(TokenRefreshRequest request) {
        String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", request.getRefreshToken());

        try {
            return restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(JwtResponse.class);
        } catch (Exception e) {
            log.error("Keycloak token refresh request failed: {}", e.getMessage());
            throw new RuntimeException("Invalid refresh token");
        }
    }
}
