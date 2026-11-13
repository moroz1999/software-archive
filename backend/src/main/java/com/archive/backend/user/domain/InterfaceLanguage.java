package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

/**
 * The interface language a visitor uses; letters to them are written in it.
 */
@ValueObject
public enum InterfaceLanguage {
    EN("en"),
    RU("ru");

    private final String code;

    InterfaceLanguage(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    /** Any language the archive does not speak falls back to English, as the interface does. */
    public static InterfaceLanguage ofCode(String code) {
        for (InterfaceLanguage language : values()) {
            if (language.code.equals(code)) {
                return language;
            }
        }
        return EN;
    }
}
