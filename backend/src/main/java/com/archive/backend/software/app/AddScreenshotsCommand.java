package com.archive.backend.software.app;

import com.archive.backend.software.domain.Gallery;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** Screenshots for the software itself, or for its release when one is named. */
public record AddScreenshotsCommand(
        Actor actor,
        SoftwareId softwareId,
        @Nullable ReleaseId releaseId,
        List<ScreenshotImage> images,
        Gallery gallery) {

    public AddScreenshotsCommand {
        if (releaseId != null && gallery != Gallery.SCREENSHOTS) {
            throw new IllegalArgumentException("A release has screenshots only.");
        }
        images = List.copyOf(images);
    }

    public AddScreenshotsCommand(
            Actor actor, SoftwareId softwareId, @Nullable ReleaseId releaseId, List<ScreenshotImage> images) {
        this(actor, softwareId, releaseId, images, Gallery.SCREENSHOTS);
    }
}
