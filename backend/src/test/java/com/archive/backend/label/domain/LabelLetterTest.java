package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.error.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LabelLetterTest {

    @ParameterizedTest
    @CsvSource({"A, A", "z, Z", "Б, Б", "я, Я", "#, #"})
    void acceptsALatinOrCyrillicLetterOrTheRestSignInAnyCase(String raw, String letter) {
        assertThat(new LabelLetter(raw).value()).isEqualTo(letter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ё", "ё"})
    void countsYoAsYe(String raw) {
        assertThat(new LabelLetter(raw)).isEqualTo(new LabelLetter("Е"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "AB", "1", "Ä", "-"})
    void refusesAnythingElse(String raw) {
        assertThatThrownBy(() -> new LabelLetter(raw))
                .isInstanceOfSatisfying(InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("label.letter-invalid"));
    }

    @Test
    void lettersOfTheSameValueAreEqual() {
        assertThat(new LabelLetter("a")).isEqualTo(new LabelLetter("A"));
    }
}
