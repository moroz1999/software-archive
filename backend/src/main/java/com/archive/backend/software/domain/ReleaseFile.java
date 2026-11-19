package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** One node of the parsed tree of a release file. */
@ValueObject
public record ReleaseFile(
        ReleaseFileId id,
        ReleaseFileName name,
        @Nullable ReleaseFileHash md5,
        boolean directory,
        boolean executable,
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
    }
}
