package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.List;

/** Screenshots are gone from the archive: their pictures are to be deleted once the change is stored. */
public record ScreenshotsDiscarded(List<ScreenshotId> screenshots) implements DomainEvent {

    public ScreenshotsDiscarded {
        screenshots = List.copyOf(screenshots);
    }
}
