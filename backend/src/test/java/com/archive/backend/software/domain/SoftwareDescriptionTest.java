package com.archive.backend.software.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SoftwareDescriptionTest {

    @Test
    void keepsTheText() {
        assertThat(new SoftwareDescription("A space trading game.").value()).isEqualTo("A space trading game.");
    }
}
