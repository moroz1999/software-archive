package com.archive.backend.label.read;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The SQL condition of a catalog letter on a name column, shared by the catalogs of names (docs/domain/authors.md):
 * a letter in both cases, Е also standing for Ё, or the rest sign for names starting with anything else.
 */
public final class LetterFilterSql {

    private static final String REST_LETTER = "#";
    /** First characters that are catalog letters; anything else is under the rest sign. */
    private static final String LETTERS_PATTERN = "^[A-Za-zА-Яа-яЁё]";

    private LetterFilterSql() {}

    /** The condition on {@code column} for {@code letter}, adding the parameters it needs to {@code params}. */
    public static String condition(String column, String letter, Map<String, Object> params) {
        if (REST_LETTER.equals(letter)) {
            params.put("lettersPattern", LETTERS_PATTERN);
            return column + " !~ :lettersPattern";
        }
        params.put("firstChars", firstChars(letter));
        return "left(" + column + ", 1) IN (:firstChars)";
    }

    private static List<String> firstChars(String letter) {
        String lower = letter.toLowerCase(Locale.ROOT);
        return "Е".equals(letter) ? List.of(letter, lower, "Ё", "ё") : List.of(letter, lower);
    }
}
