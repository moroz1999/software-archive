package com.archive.backend.software.app;

import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.SoftwareId;
import org.jspecify.annotations.Nullable;

/** The screenshots of the software itself, or of its release when one is named. */
public record ListScreenshotsQuery(
        SoftwareId softwareId, @Nullable ReleaseId releaseId) {}
