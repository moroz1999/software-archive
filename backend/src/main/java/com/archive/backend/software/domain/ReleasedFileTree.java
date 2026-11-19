package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** The parsed tree of the file of a release, with the software the release belongs to. */
@ValueObject
public record ReleasedFileTree(SoftwareId software, ReleaseFile root) {

    public ReleasedFileTree {
        Objects.requireNonNull(software, "software");
        Objects.requireNonNull(root, "root");
    }
}
