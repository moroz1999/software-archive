package com.archive.backend.user.web;

import com.archive.backend.shared.config.AuthProperties;
import com.archive.backend.user.domain.session.IssuedSession;
import com.archive.backend.user.domain.session.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * The refresh secret is kept in an HTTP-only cookie available only to the session endpoints. A session not remembered
 * keeps it until the browser closes.
 */
@Component
@RequiredArgsConstructor
public class SessionCookies {

    public static final String REFRESH_COOKIE = "refresh_token";
    private static final String REFRESH_PATH = "/api/auth";

    private final AuthProperties properties;

    public HttpHeaders issue(IssuedSession session) {
        ResponseCookie.ResponseCookieBuilder refresh =
                cookie(REFRESH_COOKIE, session.refresh().value(), REFRESH_PATH);
        if (session.remember().value()) {
            refresh.maxAge(Session.LIFETIME);
        }
        return headers(refresh.build());
    }

    public HttpHeaders clear() {
        return headers(cookie(REFRESH_COOKIE, "", REFRESH_PATH)
                .maxAge(java.time.Duration.ZERO)
                .build());
    }

    private ResponseCookie.ResponseCookieBuilder cookie(String name, String value, String path) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(properties.secureCookies())
                .sameSite("Strict")
                .path(path);
    }

    private static HttpHeaders headers(ResponseCookie refresh) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, refresh.toString());
        return headers;
    }
}
