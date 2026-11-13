package com.archive.backend.user.app;

import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.session.RefreshSecret;
import org.jspecify.annotations.Nullable;

/**
 * Who is signed in, asked by the actor of a request; {@code refresh} is the refresh secret the browser holds, if any:
 * with it, a request without a valid access token may still have a session to refresh.
 */
public record GetCurrentUserQuery(Actor actor, @Nullable RefreshSecret refresh) {}
