package com.archive.backend.software.read;

import java.nio.file.Path;

/** The stored picture of a screenshot and its media type ({@code image/png}). */
public record ScreenshotFile(Path path, String mediaType) {}
