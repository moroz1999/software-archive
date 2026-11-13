package com.archive.backend.software.read;

import java.nio.file.Path;

/** A stored release archive and the original name used when it is downloaded. */
public record ReleaseArchiveFile(Path path, String fileName, long size) {}
