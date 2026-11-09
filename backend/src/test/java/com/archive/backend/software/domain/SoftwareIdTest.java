package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SoftwareIdTest {

    @Test
    void idsWithTheSameUuidAreEqual() {
        UUID uuid = UUID.randomUUID();

        assertThat(new SoftwareId(uuid)).isEqualTo(new SoftwareId(uuid));
    }
}
