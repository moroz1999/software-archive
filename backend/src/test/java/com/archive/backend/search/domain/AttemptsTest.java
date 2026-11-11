package com.archive.backend.search.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

/** A failed search task is retried with a growing delay (docs/features/search.md#synchronization). */
class AttemptsTest {

    @Test
    void doublesTheDelayWithEveryFailureFromOneSecond() {
        assertThat(new Attempts(1).retryDelay()).isEqualTo(Duration.ofSeconds(1));
        assertThat(new Attempts(2).retryDelay()).isEqualTo(Duration.ofSeconds(2));
        assertThat(new Attempts(5).retryDelay()).isEqualTo(Duration.ofSeconds(16));
    }

    @Test
    void neverWaitsLongerThanTenMinutes() {
        assertThat(new Attempts(11).retryDelay()).isEqualTo(Duration.ofMinutes(10));
        assertThat(new Attempts(1000).retryDelay()).isEqualTo(Duration.ofMinutes(10));
    }
}
