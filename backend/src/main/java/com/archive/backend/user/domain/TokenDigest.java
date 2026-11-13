package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

/**
 * The SHA-256 digest of a secret, hex-encoded: what the archive keeps of the secrets it hands out, so its storage alone
 * opens nothing.
 */
@ValueObject
public record TokenDigest(String value) {

    private static final SecureRandom RANDOM = new SecureRandom();

    public TokenDigest {
        Objects.requireNonNull(value, "value");
    }

    public static TokenDigest of(String secret) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
            return new TokenDigest(HexFormat.of().formatHex(digest));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Every Java platform has SHA-256", e);
        }
    }

    /** A new random secret of 256 bits, URL-safe. */
    public static String randomSecret() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
