package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/**
 * One person, group or exact label credited on software. A label is the same participant whichever kind of label it
 * is taken for: the kind follows whoever owns the label now (docs/domain/software.md#credits).
 */
@ValueObject
public record SoftwareReference(SoftwareReferenceType type, SoftwareReferenceId id) {

    public SoftwareReference {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(id, "id");
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SoftwareReference that && kind() == that.kind() && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind(), id);
    }

    /** The kind that tells participants apart: both kinds of label are a label. */
    private SoftwareReferenceType kind() {
        return type == SoftwareReferenceType.GROUP_LABEL ? SoftwareReferenceType.AUTHOR_LABEL : type;
    }
}
