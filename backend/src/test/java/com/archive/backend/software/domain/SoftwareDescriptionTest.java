package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SoftwareDescriptionTest {

    @Test
    void keepsTheText() {
        assertThat(new SoftwareDescription("A space trading game.").value()).isEqualTo("A space trading game.");
    }
}
