package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.shared.domain.Title;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoftwareTitleTest {

    @Test
    void isATitle() {
        assertThat(new SoftwareTitle("Elite")).isInstanceOf(Title.class);
    }

    @Test
    void followsTheTitleRules() {
        assertThatThrownBy(() -> new SoftwareTitle("a".repeat(256)))
                .isInstanceOfSatisfying(InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("title.too-long"));
    }

    @Test
    void equalTitlesAreEqual() {
        assertThat(new SoftwareTitle("Elite")).isEqualTo(new SoftwareTitle("Elite"));
    }
}
