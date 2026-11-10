package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ReleaseTypeTest {

    @ParameterizedTest
    @EnumSource(ReleaseType.class)
    void isKnownByItsCode(ReleaseType type) {
        assertThat(ReleaseType.fromCode(type.code())).isEqualTo(type);
    }

    @Test
    void codesTheDemoVersionAsOneWord() {
        assertThat(ReleaseType.fromCode("demoversion")).isEqualTo(ReleaseType.DEMOVERSION);
    }

    @Test
    void refusesAnUnknownCode() {
        assertThatThrownBy(() -> ReleaseType.fromCode("beta"))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("release.type-invalid"));
    }
}
