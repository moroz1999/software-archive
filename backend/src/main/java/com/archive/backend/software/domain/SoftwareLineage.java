package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Where the work comes from (docs/domain/software.md#lineage): every part may be unknown, the reliability and the
 * rework only with a kind.
 */
@ValueObject
public record SoftwareLineage(
        @Nullable LineageKind kind,
        @Nullable LineageReliability reliability,
        Set<LineageRework> rework,
        @Nullable LineageSource source) {

    public static final SoftwareLineage UNKNOWN = new SoftwareLineage(null, null, Set.of(), null);

    public SoftwareLineage {
        rework = Set.copyOf(Objects.requireNonNull(rework, "rework"));
        if (kind == null && (reliability != null || !rework.isEmpty())) {
            throw new InvalidValueException(
                    "software.lineage-kind-required", "A reliability or a rework tells of a kind, which is missing.");
        }
    }
}
