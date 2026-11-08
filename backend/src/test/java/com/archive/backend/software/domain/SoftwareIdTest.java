package com.archive.backend.software.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SoftwareIdTest {

    @Test
    void idsWithTheSameUuidAreEqual() {
        UUID uuid = UUID.randomUUID();

        assertThat(new SoftwareId(uuid)).isEqualTo(new SoftwareId(uuid));
    }
}
