package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.List;

/** Release archives gone from the catalog whose files must go after the change commits. */
public record ReleaseArchivesDiscarded(List<ReleaseArchiveId> archives) implements DomainEvent {

    public ReleaseArchivesDiscarded {
        archives = List.copyOf(archives);
    }
}
