package com.archive.backend.author.domain;

import com.archive.backend.shared.domain.error.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthorLetterTest {

    @ParameterizedTest
    @CsvSource({"A, A", "z, Z", "Б, Б", "я, Я", "#, #"})
    void acceptsALatinOrCyrillicLetterOrTheRestSignInAnyCase(String raw, String letter) {
        assertThat(new AuthorLetter(raw).value()).isEqualTo(letter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ё", "ё"})
    void countsYoAsYe(String raw) {
        assertThat(new AuthorLetter(raw)).isEqualTo(new AuthorLetter("Е"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "AB", "1", "Ä", "-"})
    void refusesAnythingElse(String raw) {
        assertThatThrownBy(() -> new AuthorLetter(raw))
                .isInstanceOfSatisfying(InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("author.letter-invalid"));
    }

    @Test
    void lettersOfTheSameValueAreEqual() {
        assertThat(new AuthorLetter("a")).isEqualTo(new AuthorLetter("A"));
    }
}
