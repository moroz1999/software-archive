package com.archive.backend.assistant.web.software;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of delete_screenshot: a screenshot of the software, or of its release when one is named. */
public record DeleteScreenshotArguments(
        UUID softwareId, @Nullable UUID releaseId, UUID screenshotId) {}
