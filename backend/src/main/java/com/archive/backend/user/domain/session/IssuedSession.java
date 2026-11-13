package com.archive.backend.user.domain.session;

import java.time.Instant;

/**
 * What a browser gets on signing in or refreshing: the access token and the refresh secret of the session.
 */
public record IssuedSession(AccessToken access, RefreshSecret refresh, RememberMe remember, Instant expiresAt) {}
