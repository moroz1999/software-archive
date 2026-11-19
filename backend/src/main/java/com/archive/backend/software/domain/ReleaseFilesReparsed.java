package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** How an explicit reparse of every stored release file went: how many files were parsed and how many failed. */
@ValueObject
public record ReleaseFilesReparsed(int parsed, int failed) {

    public ReleaseFilesReparsed {
        if (parsed < 0 || failed < 0) {
            throw new IllegalArgumentException("A count of release files cannot be negative");
        }
    }
}
