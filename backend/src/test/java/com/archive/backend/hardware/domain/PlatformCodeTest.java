package com.archive.backend.hardware.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PlatformCodeTest {

    @Test
    void holdsACode() {
        assertThat(new PlatformCode("vector").value()).isEqualTo("vector");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "Vector", "zx spectrum", "bk!"})
    void refusesWhatIsNotACode(String raw) {
        assertThatThrownBy(() -> new PlatformCode(raw))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("platform.code-invalid"));
    }
}
