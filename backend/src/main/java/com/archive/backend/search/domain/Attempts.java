package com.archive.backend.search.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.time.Duration;

/** How many times bringing a search document up to date has failed so far. */
@ValueObject
public record Attempts(int value) {

    private static final Duration FIRST_DELAY = Duration.ofSeconds(1);
    private static final Duration LONGEST_DELAY = Duration.ofMinutes(10);

    /** The wait before the next try after this many failures: doubled with every failure, at most ten minutes. */
    public Duration retryDelay() {
        int doublings = Math.max(0, value - 1);
        if (doublings >= 30) {
            return LONGEST_DELAY;
        }
        Duration delay = FIRST_DELAY.multipliedBy(1L << doublings);
        return delay.compareTo(LONGEST_DELAY) > 0 ? LONGEST_DELAY : delay;
    }

    public Attempts next() {
        return new Attempts(value + 1);
    }
}
