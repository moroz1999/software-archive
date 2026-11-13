package com.archive.backend.software.app;

import com.archive.backend.software.domain.ScreenshotPictureRepository;
import com.archive.backend.software.domain.ScreenshotsDiscarded;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/** Deletes the pictures of screenshots gone from the archive, once the change that discarded them commits. */
@Service
@RequiredArgsConstructor
public class DiscardScreenshotFilesHandler {

    private final ScreenshotPictureRepository files;

    @EventListener
    public void on(ScreenshotsDiscarded discarded) {
        files.discard(discarded.screenshots());
    }
}
