package com.archive.backend.author.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import org.junit.jupiter.api.Test;

class RealNameTest {

    @Test
    void keepsTheName() {
        assertThat(new RealName("John Doe").value()).isEqualTo("John Doe");
    }

    @Test
    void cannotBeBlank() {
        assertThatThrownBy(() -> new RealName("  "))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("author.real-name-blank"));
    }

    @Test
    void isAtMost255CharactersLong() {
        assertThat(new RealName("a".repeat(255)).value()).hasSize(255);
        assertThatThrownBy(() -> new RealName("a".repeat(256)))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("author.real-name-too-long"));
    }
}
