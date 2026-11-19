package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** One node of the parsed tree of a release file; a root that could not be parsed whole is unreadable. */
@ValueObject
public record ReleaseFile(
        ReleaseFileId id,
        ReleaseFileName name,
        @Nullable ReleaseFileHash md5,
        boolean directory,
        boolean executable,
        boolean unreadable,
        List<ReleaseFile> children) {

    public ReleaseFile {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        children = List.copyOf(children);
        if (directory && (md5 != null || executable)) {
            throw new IllegalArgumentException("A directory has no hash and cannot be executable.");
        }
        if (!directory && md5 == null) {
            throw new IllegalArgumentException("Every release file needs a hash.");
        }
        if (unreadable && (directory || !children.isEmpty())) {
            throw new IllegalArgumentException("Only a file can be unreadable, and it has no known contents.");
        }
    }
}
