package com.archive.backend.user.domain.session;

import com.archive.backend.user.domain.User;
import java.time.Instant;

/**
 * Issues access tokens.
 */
public interface AccessTokens {

    AccessToken issue(User user, Instant now);
}
