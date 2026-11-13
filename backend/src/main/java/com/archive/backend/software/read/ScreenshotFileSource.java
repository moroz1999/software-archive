package com.archive.backend.software.read;

import java.util.Optional;
import java.util.UUID;

/** Finds the stored picture of a screenshot; empty when there is no such screenshot. */
public interface ScreenshotFileSource {

    Optional<ScreenshotFile> find(UUID screenshotId);
}
