package com.archive.backend.software.domain;

import java.util.List;
import java.util.function.Predicate;

/** A catalog of credited people, groups and labels for tests: whoever the predicate knows, none of them with labels. */
public record KnownReferences(Predicate<SoftwareReference> known) implements SoftwareReferenceCatalog {

    public static SoftwareCreditSets creditSets(Predicate<SoftwareReference> known) {
        return new SoftwareCreditSets(new KnownReferences(known));
    }

    @Override
    public boolean contains(SoftwareReference reference) {
        return known.test(reference);
    }

    @Override
    public List<SoftwareReferenceId> labelsOf(SoftwareReference owner) {
        return List.of();
    }
}
