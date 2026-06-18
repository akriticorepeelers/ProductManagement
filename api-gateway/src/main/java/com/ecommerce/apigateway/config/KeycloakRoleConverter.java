package com.ecommerce.apigateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * KeycloakRoleConverter extracts Keycloak realm roles (e.g., ADMIN, CUSTOMER)
 * from the token's 'realm_access' claim and maps them to Spring Security 'ROLE_' authorities.
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @SuppressWarnings("unchecked")
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");

        if (realmAccess == null || realmAccess.isEmpty()) {
            return List.of();
        }

        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        if (roles == null) {
            return List.of();
        }

        return roles.stream()
                .map(roleName -> "ROLE_" + roleName) // Prefix with ROLE_ for Spring Security
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
