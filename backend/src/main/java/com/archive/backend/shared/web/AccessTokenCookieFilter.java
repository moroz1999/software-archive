package com.archive.backend.shared.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Signs the request in by the access token of its cookie. A missing, expired or forged token leaves the request
 * anonymous: what needs a user refuses it later, and the browser refreshes its session then.
 */
public class AccessTokenCookieFilter extends OncePerRequestFilter {

    public static final String COOKIE = "access_token";

    private final JwtDecoder decoder;

    public AccessTokenCookieFilter(JwtDecoder decoder) {
        this.decoder = decoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = token(request);
        if (token != null) {
            try {
                Jwt jwt = decoder.decode(token);
                SecurityContextHolder.getContext()
                        .setAuthentication(UsernamePasswordAuthenticationToken.authenticated(jwt, "", List.of()));
            } catch (JwtException e) {
                logger.debug("Access token refused, the request goes on anonymous", e);
            }
        }
        chain.doFilter(request, response);
    }

    private static @Nullable String token(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE.equals(cookie.getName()) && !cookie.getValue().isEmpty()) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
