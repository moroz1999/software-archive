package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.UUID;

@ValueObject
public record ReleaseArchiveId(UUID value) {

    public ReleaseArchiveId {
        if (value == null) {
            throw new IllegalArgumentException("A release archive id cannot be null.");
        }
    }
}
