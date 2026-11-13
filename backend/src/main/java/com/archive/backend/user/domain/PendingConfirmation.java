package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.time.Instant;

/**
 * The confirmation link an unconfirmed account waits for: the digest of its secret and when it stops working.
 */
@ValueObject
public record PendingConfirmation(TokenDigest digest, Instant expiresAt) {}
