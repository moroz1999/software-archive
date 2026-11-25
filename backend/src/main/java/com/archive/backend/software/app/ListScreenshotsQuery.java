package com.archive.backend.software.app;

import com.archive.backend.software.domain.Gallery;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.SoftwareId;
import org.jspecify.annotations.Nullable;

/** The screenshots of the software itself, or of its release when one is named. */
public record ListScreenshotsQuery(
        SoftwareId softwareId, @Nullable ReleaseId releaseId, Gallery gallery) {

    public ListScreenshotsQuery {
        if (releaseId != null && gallery != Gallery.SCREENSHOTS) {
            throw new IllegalArgumentException("A release has screenshots only.");
        }
    }

    public ListScreenshotsQuery(SoftwareId softwareId, @Nullable ReleaseId releaseId) {
        this(softwareId, releaseId, Gallery.SCREENSHOTS);
    }
}
