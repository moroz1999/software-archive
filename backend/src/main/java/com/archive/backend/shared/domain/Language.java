package com.archive.backend.shared.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Optional;

/**
 * A language of the site: of its interface, of the letters to its users and of the names kept in several languages
 * (docs/domain/localization.md). Russian is the language every such name has.
 */
@ValueObject
public enum Language {
    EN("en"),
    RU("ru");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    /**
     * The language a visitor asked for; one the archive does not speak falls back to English, as the interface does.
     */
    public static Language ofCode(String code) {
        return find(code).orElse(EN);
    }

    /** A language a name is given in: only a language of the site. */
    public static Language known(String code) {
        return find(code)
                .orElseThrow(() ->
                        new InvalidValueException("language.unknown", "The site does not speak the language " + code));
    }

    /** The language of the site with the given code, if the site speaks it. */
    public static Optional<Language> find(String code) {
        for (Language language : values()) {
            if (language.code.equals(code)) {
                return Optional.of(language);
            }
        }
        return Optional.empty();
    }
}
