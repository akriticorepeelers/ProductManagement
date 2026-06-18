package com.ecommerce.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * User entity represents a local reference of registered users.
 * The primary identifier in the system is 'keycloakId', which links to Keycloak's UUID.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_id", nullable = false, unique = true)
    private String keycloakId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String role; // ADMIN or CUSTOMER
}
