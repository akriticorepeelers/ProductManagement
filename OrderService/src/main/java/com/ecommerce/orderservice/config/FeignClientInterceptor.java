package com.ecommerce.orderservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * FeignClientInterceptor propagates JWT Bearer tokens in out-of-process Feign requests.
 * Reads the token from SecurityContextHolder.
 */
@Configuration
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String tokenValue = jwtAuthenticationToken.getToken().getTokenValue();
            template.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue);
        }
    }
}
