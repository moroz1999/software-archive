package com.archive.backend.shared.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings of accounts and sessions.
 *
 * @param jwtSecret the HMAC key access tokens are signed with, at least 32 bytes
 * @param accessLifetime how long an access token works
 * @param publicUrl the address of the frontend, which confirmation links lead to
 * @param mailFrom the sender of letters to users
 * @param secureCookies whether session cookies go over HTTPS only
 */
@ConfigurationProperties("auth")
public record AuthProperties(
        String jwtSecret, Duration accessLifetime, String publicUrl, String mailFrom, boolean secureCookies) {}
