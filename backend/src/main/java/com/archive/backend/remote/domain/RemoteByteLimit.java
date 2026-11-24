package com.archive.backend.remote.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** How many bytes a download from another site may have at most. */
@ValueObject
public record RemoteByteLimit(long value) {

    public RemoteByteLimit {
        if (value <= 0) {
            throw new IllegalArgumentException("A limit is positive: " + value);
        }
    }
}
