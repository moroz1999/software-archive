package com.archive.backend.user.app;

import com.archive.backend.user.domain.session.RefreshSecret;
import org.jspecify.annotations.Nullable;

/** The secret is missing when the browser holds no session any more: nothing to close then. */
public record LogOutCommand(@Nullable RefreshSecret secret) {}
