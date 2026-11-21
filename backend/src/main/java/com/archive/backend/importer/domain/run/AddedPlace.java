package com.archive.backend.importer.domain.run;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** A city or a country a run added to the archive, under the name the source gave. */
@ValueObject
public record AddedPlace(Kind kind, String name) {

    /** Which of the two the place is. */
    public enum Kind {
        CITY,
        COUNTRY
    }

    public AddedPlace {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(name, "name");
    }
}
