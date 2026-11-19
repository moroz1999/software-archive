package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.List;
import java.util.Objects;

/** The parsed structures of these releases no longer match their files: a file was added, replaced or removed, or the platform deciding what is executable changed. */
public record ReleaseFileStructuresOutdated(List<ReleaseId> releases) implements DomainEvent {

    public ReleaseFileStructuresOutdated {
        releases = List.copyOf(Objects.requireNonNull(releases, "releases"));
    }
}
