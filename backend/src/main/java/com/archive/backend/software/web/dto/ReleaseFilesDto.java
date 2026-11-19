package com.archive.backend.software.web.dto;

import org.jspecify.annotations.Nullable;

/** The parsed tree of a release file; no root while the file waits for parsing. */
public record ReleaseFilesDto(@Nullable ReleaseFileDto root) {}
