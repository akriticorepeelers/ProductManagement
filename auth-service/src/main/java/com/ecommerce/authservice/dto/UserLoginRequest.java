package com.ecommerce.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * UserLoginRequest encapsulates credentials for user authentication.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
