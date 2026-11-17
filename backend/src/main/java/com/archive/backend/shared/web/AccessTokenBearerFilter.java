package com.archive.backend.shared.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

/** Signs a request in by its Bearer access token and rejects an invalid authentication attempt. */
public class AccessTokenBearerFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final JwtDecoder decoder;

    public AccessTokenBearerFilter(JwtDecoder decoder) {
        this.decoder = decoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null) {
            chain.doFilter(request, response);
            return;
        }

        String token = bearerToken(authorization);
        if (token == null) {
            unauthorized(response);
            return;
        }
        try {
            Jwt jwt = decoder.decode(token);
            SecurityContextHolder.getContext()
                    .setAuthentication(UsernamePasswordAuthenticationToken.authenticated(jwt, "", List.of()));
            chain.doFilter(request, response);
        } catch (JwtException e) {
            logger.debug("Bearer access token refused", e);
            unauthorized(response);
        }
    }

    private static @Nullable String bearerToken(String authorization) {
        if (!authorization.startsWith(PREFIX)) {
            return null;
        }
        String token = authorization.substring(PREFIX.length());
        return token.isBlank() ? null : token;
    }

    private static void unauthorized(HttpServletResponse response) throws IOException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"invalid_token\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                        {"type":"about:blank","title":"Unauthorized","status":401,
                         "detail":"The access token is invalid or expired.","code":"auth.access-token-invalid"}
                        """);
    }
}
