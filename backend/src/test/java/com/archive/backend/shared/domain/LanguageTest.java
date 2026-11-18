package com.archive.backend.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import org.junit.jupiter.api.Test;

class LanguageTest {

    @Test
    void asksForAnUnknownLanguageInEnglish() {
        assertThat(Language.ofCode("ru")).isEqualTo(Language.RU);
        assertThat(Language.ofCode("de")).isEqualTo(Language.EN);
    }

    @Test
    void takesTitlesOnlyInALanguageOfTheSite() {
        assertThat(Language.known("ru")).isEqualTo(Language.RU);
        assertThat(Language.known("en")).isEqualTo(Language.EN);
        assertThatThrownBy(() -> Language.known("de"))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("language.unknown"));
    }
}
