package com.archive.backend.software.read;

import java.util.UUID;

/** The downloadable archive of a release, with its visitor-facing metadata. */
public record ReleaseArchiveDetails(UUID id, String fileName, long size) {}
