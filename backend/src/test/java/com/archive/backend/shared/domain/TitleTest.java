package com.archive.backend.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import org.junit.jupiter.api.Test;

class TitleTest {

    private static final class BookTitle extends Title {
        BookTitle(String value) {
            super(value);
        }
    }

    private static final class FilmTitle extends Title {
        FilmTitle(String value) {
            super(value);
        }
    }

    @Test
    void keepsTheTitle() {
        assertThat(new BookTitle("Elite").value()).isEqualTo("Elite");
    }

    @Test
    void rejectsABlankTitle() {
        assertThatThrownBy(() -> new BookTitle("  "))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("title.required"));
    }

    @Test
    void acceptsATitleOf255Characters() {
        assertThat(new BookTitle("a".repeat(255)).value()).hasSize(255);
    }

    @Test
    void rejectsATitleLongerThan255Characters() {
        assertThatThrownBy(() -> new BookTitle("a".repeat(256)))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("title.too-long"));
    }

    @Test
    void isEqualOnlyToTheSameKindOfTitleWithTheSameValue() {
        assertThat(new BookTitle("Elite")).isEqualTo(new BookTitle("Elite")).hasSameHashCodeAs(new BookTitle("Elite"));
        assertThat(new BookTitle("Elite")).isNotEqualTo(new BookTitle("Exolon"));
        assertThat(new BookTitle("Elite")).isNotEqualTo(new FilmTitle("Elite"));
    }
}
