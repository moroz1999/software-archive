package com.archive.backend.software.read;

import org.jspecify.annotations.Nullable;

/** Whether a release has a file and, once parsed, the tree rooted at it; no root while the file waits for parsing. */
public record ReleaseFiles(boolean archive, @Nullable ReleaseFileItem root) {}
