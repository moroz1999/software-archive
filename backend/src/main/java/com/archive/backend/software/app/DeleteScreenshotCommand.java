package com.archive.backend.software.app;

import com.archive.backend.software.domain.Gallery;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import org.jspecify.annotations.Nullable;

public record DeleteScreenshotCommand(
        Actor actor, SoftwareId softwareId, @Nullable ReleaseId releaseId, ScreenshotId screenshotId, Gallery gallery) {

    public DeleteScreenshotCommand {
        if (releaseId != null && gallery != Gallery.SCREENSHOTS) {
            throw new IllegalArgumentException("A release has screenshots only.");
        }
    }

    public DeleteScreenshotCommand(
            Actor actor, SoftwareId softwareId, @Nullable ReleaseId releaseId, ScreenshotId screenshotId) {
        this(actor, softwareId, releaseId, screenshotId, Gallery.SCREENSHOTS);
    }
}
