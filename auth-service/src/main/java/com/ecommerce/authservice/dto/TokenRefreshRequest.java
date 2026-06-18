package com.ecommerce.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * TokenRefreshRequest wraps the refresh token parameter required to issue new access tokens.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
