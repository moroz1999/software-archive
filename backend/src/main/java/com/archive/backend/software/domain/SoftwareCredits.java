package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Everyone credited on one software item, each once. */
@ValueObject
public final class SoftwareCredits {

    public static final SoftwareCredits EMPTY = new SoftwareCredits(List.of());

    private final List<SoftwareCreditDraft> credits;

    private SoftwareCredits(List<SoftwareCreditDraft> credits) {
        this.credits = List.copyOf(credits);
        Set<SoftwareReference> participants = new HashSet<>();
        if (credits.stream().anyMatch(credit -> !participants.add(credit.participant()))) {
            throw new InvalidValueException("software.credit-duplicate", "Anyone can be credited only once.");
        }
    }

    static SoftwareCredits of(List<SoftwareCreditDraft> credits) {
        return new SoftwareCredits(credits);
    }

    public static SoftwareCredits restore(List<SoftwareCreditDraft> credits) {
        return new SoftwareCredits(credits);
    }

    public List<SoftwareCreditDraft> all() {
        return credits;
    }
}
