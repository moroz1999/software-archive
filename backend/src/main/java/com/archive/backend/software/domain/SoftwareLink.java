package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** An external link of software: an address and what it is, such as its homepage or a video of it. */
@ValueObject
public record SoftwareLink(SoftwareLinkType type, SoftwareUrl url) {

    public SoftwareLink {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(url, "url");
    }
}
