package com.archive.backend.importer.domain.source;

import com.archive.backend.shared.domain.EnumCodes;
import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/**
 * Something of a program the import could not take as the source gives it, for the administrator to look at: a value
 * the import does not know, a date it cannot read, a file the source lacks (docs/domain/imports.md). The program is
 * taken all the same, without it.
 *
 * @param value what the source gives, as it gives it
 */
@ValueObject
public record SourceWarning(Kind kind, String value) {

    public SourceWarning {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(value, "value");
    }

    /** What could not be taken. */
    public enum Kind {
        PLATFORM,
        GENRE,
        DATE,
        LINEAGE,
        LINEAGE_SOURCE_TOO_LONG,
        FILE_MISSING,
        SCREENSHOT_MISSING;

        /** The code it is known by outside the backend: {@code FILE_MISSING} as {@code file-missing}. */
        public String code() {
            return EnumCodes.kebab(this);
        }
    }
}
