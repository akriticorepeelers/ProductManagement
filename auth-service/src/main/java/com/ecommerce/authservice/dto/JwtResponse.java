package com.ecommerce.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * JwtResponse holds the standard OAuth2 token parameters returned from Keycloak token exchange.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("refresh_expires_in")
    private Long refreshExpiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("scope")
    private String scope;
}
