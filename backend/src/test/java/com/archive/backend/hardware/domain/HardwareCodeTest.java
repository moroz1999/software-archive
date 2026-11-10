package com.archive.backend.hardware.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HardwareCodeTest {

    @ParameterizedTest
    @ValueSource(strings = {"ay8910", "bk0010-01"})
    void holdsACode(String raw) {
        assertThat(new HardwareCode(raw).value()).isEqualTo(raw);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "AY8910", "sound card", "ay!"})
    void refusesWhatIsNotACode(String raw) {
        assertThatThrownBy(() -> new HardwareCode(raw))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("hardware.code-invalid"));
    }
}
