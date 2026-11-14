package com.archive.backend.importer.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** The absolute address of the page an imported entry has in its source archive. */
@ValueObject
public record ExternalPage(String url) {

    public ExternalPage {
        Objects.requireNonNull(url, "url");
    }
}
