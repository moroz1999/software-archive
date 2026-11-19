package com.archive.backend.software.read;

import java.util.List;
import java.util.UUID;

/**
 * One node of the release file tree shown to visitors; a file that could not be read is shown without contents, marked
 * as such.
 */
public record ReleaseFileItem(
        UUID id,
        String name,
        boolean directory,
        boolean executable,
        boolean unreadable,
        List<ReleaseFileItem> children) {}
