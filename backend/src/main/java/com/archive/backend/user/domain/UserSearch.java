package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import org.jspecify.annotations.Nullable;

/**
 * A part of the user name or email the list of users is narrowed to.
 */
@ValueObject
public record UserSearch(String value) {

    /**
     * The search typed, without surrounding spaces; nothing typed searches nothing.
     */
    public static @Nullable UserSearch of(@Nullable String typed) {
        return typed == null || typed.isBlank() ? null : new UserSearch(typed.strip());
    }
}
