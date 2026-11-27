package com.archive.backend.shared.domain;

import java.util.Locale;

/** How the constant of an enum without codes of its own is known outside the backend. */
public final class EnumCodes {

    private EnumCodes() {}

    /** {@code TOOL_CALL} as {@code toolCall}. */
    public static String camel(Enum<?> constant) {
        StringBuilder out = new StringBuilder();
        boolean upper = false;
        for (char letter : constant.name().toLowerCase(Locale.ROOT).toCharArray()) {
            if (letter == '_') {
                upper = true;
            } else {
                out.append(upper ? Character.toUpperCase(letter) : letter);
                upper = false;
            }
        }
        return out.toString();
    }

    /** {@code RELEASE_DATE} as {@code release-date}. */
    public static String kebab(Enum<?> constant) {
        return constant.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
