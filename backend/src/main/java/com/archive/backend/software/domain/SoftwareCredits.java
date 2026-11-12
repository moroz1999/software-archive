package com.archive.backend.software.domain;

import com.archive.backend.author.domain.PersonReference;
import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The complete set of people credited by one software item. */
@ValueObject
public final class SoftwareCredits {

    public static final SoftwareCredits EMPTY = new SoftwareCredits(List.of());

    private final List<SoftwareCreditDraft> credits;

    private SoftwareCredits(List<SoftwareCreditDraft> credits) {
        this.credits = List.copyOf(credits);
        Set<PersonReference> people = new HashSet<>();
        if (credits.stream().anyMatch(credit -> !people.add(credit.person()))) {
            throw new InvalidValueException("software.credit-duplicate", "A person can be credited only once.");
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
