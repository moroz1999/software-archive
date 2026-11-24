package com.archive.backend.assistant.web.tool;

import org.jspecify.annotations.Nullable;

/** How a change of the fields given goes over an entry as it is: a field not given stays, an empty text clears it. */
public final class Changes {

    private Changes() {}

    public static <T> T given(@Nullable T given, T current) {
        return given == null ? current : given;
    }

    /** A text given replaces the current one; given empty, it clears it. */
    public static @Nullable String text(@Nullable String given, @Nullable String current) {
        if (given == null) {
            return current;
        }
        return given.isEmpty() ? null : given;
    }

    /** A text of a new entry: given empty, it is none. */
    public static @Nullable String text(@Nullable String given) {
        return given == null || given.isEmpty() ? null : given;
    }
}
