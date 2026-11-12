package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/** The publishers and developer companies chosen for software. */
@ValueObject
public final class SoftwareReferences {

    public static final SoftwareReferences EMPTY = new SoftwareReferences(Set.of(), Set.of());

    private final Set<SoftwareReference> publishers;
    private final Set<SoftwareReference> developers;

    SoftwareReferences(Set<SoftwareReference> publishers, Set<SoftwareReference> developers) {
        this.publishers = Set.copyOf(Objects.requireNonNull(publishers, "publishers"));
        this.developers = Set.copyOf(Objects.requireNonNull(developers, "developers"));
        if (this.developers.stream().anyMatch(reference -> !reference.type().isGroup())) {
            throw new InvalidValueException(
                    "software.reference-type-invalid", "A developer company must be a group or a group label.");
        }
    }

    public static SoftwareReferences restore(Set<SoftwareReference> publishers, Set<SoftwareReference> developers) {
        return new SoftwareReferences(publishers, developers);
    }

    public Set<SoftwareReference> publishers() {
        return publishers;
    }

    public Set<SoftwareReference> developers() {
        return developers;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof SoftwareReferences references
                && publishers.equals(references.publishers)
                && developers.equals(references.developers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publishers, developers);
    }
}
