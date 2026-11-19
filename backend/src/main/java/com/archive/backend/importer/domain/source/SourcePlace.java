package com.archive.backend.importer.domain.source;

import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/**
 * A place an author is from, as a source names it; what it is and the language of its name are told by each source
 * (docs/domain/imports.md#authors).
 */
@ValueObject
public record SourcePlace(String name, Language language, SourcePlaceKind kind) {

    public SourcePlace {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(language, "language");
        Objects.requireNonNull(kind, "kind");
    }
}
