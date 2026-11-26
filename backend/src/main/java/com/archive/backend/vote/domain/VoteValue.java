package com.archive.backend.vote.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;

/** A vote on a work: one to five stars, or none once the voter withdrew it (docs/domain/voting.md). */
@ValueObject
public record VoteValue(int value) {

    public static final int MAX = 5;
    public static final VoteValue WITHDRAWN = new VoteValue(0);

    public VoteValue {
        if (value < 0 || value > MAX) {
            throw new InvalidValueException("vote.value-invalid", "A vote is 1 to 5 stars, or 0 to withdraw it.");
        }
    }

    /** Whether the voter took the vote back: it counts for nothing. */
    public boolean isWithdrawn() {
        return value == 0;
    }
}
