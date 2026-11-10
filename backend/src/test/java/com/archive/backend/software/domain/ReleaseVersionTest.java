package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import org.junit.jupiter.api.Test;

class ReleaseVersionTest {

    @Test
    void holdsAVersionOfUpToFiftyCharacters() {
        assertThat(new ReleaseVersion("v".repeat(50)).value()).hasSize(50);
    }

    @Test
    void refusesALongerVersion() {
        assertThatThrownBy(() -> new ReleaseVersion("v".repeat(51)))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("release.version-too-long"));
    }
}
