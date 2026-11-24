package com.archive.backend.assistant.web.software;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of set_release_archive. */
public record SetReleaseArchiveArguments(
        UUID softwareId,
        UUID releaseId,
        String url,
        @Nullable String fileName) {}
