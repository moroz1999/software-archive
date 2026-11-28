package com.archive.backend.importer.domain.source;

import com.archive.backend.author.domain.RealName;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * An author of a program as a source names it: remembered by its id there, or by the label text when the source
 * gives none (docs/domain/imports.md#origin).
 *
 * @param label the name the program is credited under
 * @param realName the full name of a person, when the source tells it
 * @param roles what the author did in the program, as the codes of credit roles; unknown when the source does not tell
 */
@ValueObject
public record SourceAuthor(
        @Nullable String externalId,
        LabelName label,
        SourceAuthorKind kind,
        @Nullable RealName realName,
        @Nullable SourcePlace place,
        List<String> roles) {

    private static final List<String> UNKNOWN_ROLE = List.of("unknown");

    public SourceAuthor {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(kind, "kind");
        roles = List.copyOf(roles);
    }

    /** An author the source tells no role of. */
    public SourceAuthor(
            @Nullable String externalId,
            LabelName label,
            SourceAuthorKind kind,
            @Nullable RealName realName,
            @Nullable SourcePlace place) {
        this(externalId, label, kind, realName, place, UNKNOWN_ROLE);
    }

    /** A name the source does not tell anything more of, known by its text. */
    public static SourceAuthor label(LabelName label) {
        return new SourceAuthor(null, label, SourceAuthorKind.LABEL, null, null);
    }

    /** A name the source does not tell anything more of, known by its text, with the roles it had in the program. */
    public static SourceAuthor label(LabelName label, List<String> roles) {
        return new SourceAuthor(null, label, SourceAuthorKind.LABEL, null, null, roles);
    }

    /** What the author is remembered by on its source. */
    public String originId() {
        return externalId == null ? label.value() : externalId;
    }
}
