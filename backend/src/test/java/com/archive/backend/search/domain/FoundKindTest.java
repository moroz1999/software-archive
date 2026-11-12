package com.archive.backend.search.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import org.junit.jupiter.api.Test;

/** The kinds of what the full search finds (docs/domain/search.md#full-search). */
class FoundKindTest {

    @Test
    void listsPeopleThenGroupsThenSoftware() {
        assertThat(FoundKind.values()).containsExactly(FoundKind.AUTHOR, FoundKind.GROUP, FoundKind.SOFTWARE);
    }

    @Test
    void isKnownByItsCode() {
        assertThat(FoundKind.of("group")).isEqualTo(FoundKind.GROUP);
        assertThat(FoundKind.SOFTWARE.code()).isEqualTo("software");
    }

    @Test
    void refusesAnUnknownCode() {
        assertThatThrownBy(() -> FoundKind.of("picture"))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("search.type-invalid"));
    }
}
