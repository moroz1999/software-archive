package com.archive.backend.software.read;

import java.util.List;
import java.util.UUID;

/** One node of the release file tree shown to visitors. */
public record ReleaseFileItem(
        UUID id, String name, boolean directory, boolean executable, List<ReleaseFileItem> children) {}
