package com.archive.backend.group.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import org.junit.jupiter.api.Test;

class GroupTypeCodeTest {

    @Test
    void keepsTheCode() {
        assertThat(new GroupTypeCode("crack group").value()).isEqualTo("crack group");
    }

    @Test
    void cannotBeBlank() {
        assertThatThrownBy(() -> new GroupTypeCode(" "))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("group.type-code-blank"));
    }
}
