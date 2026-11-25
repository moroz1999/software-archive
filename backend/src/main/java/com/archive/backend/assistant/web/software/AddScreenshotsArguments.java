package com.archive.backend.assistant.web.software;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of add_screenshots: of the software, or of its release when one is named. */
public record AddScreenshotsArguments(
        UUID softwareId, @Nullable UUID releaseId, @Nullable String gallery, List<String> urls) {}
