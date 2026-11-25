package com.archive.backend.software.app;

import com.archive.backend.software.domain.Gallery;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record ReorderScreenshotsCommand(
        Actor actor, SoftwareId softwareId, @Nullable ReleaseId releaseId, List<ScreenshotId> order, Gallery gallery) {

    public ReorderScreenshotsCommand {
        if (releaseId != null && gallery != Gallery.SCREENSHOTS) {
            throw new IllegalArgumentException("A release has screenshots only.");
        }
        order = List.copyOf(order);
    }

    public ReorderScreenshotsCommand(
            Actor actor, SoftwareId softwareId, @Nullable ReleaseId releaseId, List<ScreenshotId> order) {
        this(actor, softwareId, releaseId, order, Gallery.SCREENSHOTS);
    }
}
