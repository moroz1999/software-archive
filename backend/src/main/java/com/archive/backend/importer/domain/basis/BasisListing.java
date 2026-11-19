package com.archive.backend.importer.domain.basis;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** A card of the list of "Базис": the id of the program and the absolute address of its page. */
@ValueObject
public record BasisListing(String id, String url) {

    public BasisListing {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(url, "url");
    }
}
