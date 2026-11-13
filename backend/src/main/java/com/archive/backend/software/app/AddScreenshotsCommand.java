package com.archive.backend.software.app;

import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** Screenshots for the software itself, or for its release when one is named. */
public record AddScreenshotsCommand(
        Actor actor, SoftwareId softwareId, @Nullable ReleaseId releaseId, List<ScreenshotImage> images) {

    public AddScreenshotsCommand {
        images = List.copyOf(images);
    }
}
