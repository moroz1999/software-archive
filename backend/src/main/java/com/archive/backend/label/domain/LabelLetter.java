package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Locale;
import java.util.Objects;

/**
 * A letter of the catalogs of names: a Latin letter A–Z, a Cyrillic letter А–Я (Ё counts as Е), or {@link #REST}
 * for names starting with anything else (docs/domain/authors.md). Held in upper case.
 */
@ValueObject
public record LabelLetter(String value) {

    /**
     * The letter of names that start with neither a Latin nor a Cyrillic letter.
     */
    public static final String REST = "#";

    public LabelLetter {
        Objects.requireNonNull(value, "value");
        value = normalize(value);
    }

    public boolean isRest() {
        return REST.equals(value);
    }

    private static String normalize(String raw) {
        if (REST.equals(raw)) {
            return raw;
        }
        String upper = raw.toUpperCase(Locale.ROOT);
        if ("Ё".equals(upper)) {
            return "Е";
        }
        if (upper.length() != 1 || !isCatalogLetter(upper.charAt(0))) {
            throw new InvalidValueException("label.letter-invalid", "A catalog letter is A–Z, А–Я or #.");
        }
        return upper;
    }

    private static boolean isCatalogLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'А' && c <= 'Я');
    }
}
