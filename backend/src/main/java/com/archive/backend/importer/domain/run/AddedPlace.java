package com.archive.backend.importer.domain.run;

import com.archive.backend.shared.domain.EnumCodes;
import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** A city or a country a run added to the archive, under the name the source gave. */
@ValueObject
public record AddedPlace(Kind kind, String name) {

    /** Which of the two the place is. */
    public enum Kind {
        CITY,
        COUNTRY;

        /** The code it is known by outside the backend: {@code CITY} as {@code city}. */
        public String code() {
            return EnumCodes.kebab(this);
        }
    }

    public AddedPlace {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(name, "name");
    }
}
