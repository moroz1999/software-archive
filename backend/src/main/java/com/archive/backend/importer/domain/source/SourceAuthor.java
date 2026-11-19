package com.archive.backend.importer.domain.source;

import com.archive.backend.author.domain.RealName;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * An author of a program as a source names it: remembered by its id there, or by the label text when the source
 * gives none (docs/domain/imports.md#origin).
 *
 * @param label the name the program is credited under
 * @param realName the full name of a person, when the source tells it
 */
@ValueObject
public record SourceAuthor(
        @Nullable String externalId,
        LabelName label,
        SourceAuthorKind kind,
        @Nullable RealName realName,
        @Nullable SourcePlace place) {

    public SourceAuthor {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(kind, "kind");
    }

    /** A name the source does not tell anything more of, known by its text. */
    public static SourceAuthor label(LabelName label) {
        return new SourceAuthor(null, label, SourceAuthorKind.LABEL, null, null);
    }

    /** What the author is remembered by on its source. */
    public String originId() {
        return externalId == null ? label.value() : externalId;
    }
}
