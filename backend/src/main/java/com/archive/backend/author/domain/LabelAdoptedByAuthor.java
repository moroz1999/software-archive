package com.archive.backend.author.domain;

import com.archive.backend.label.domain.LabelId;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.Objects;

/**
 * A person took a label found out to be theirs: wherever they are credited by themselves and under the label, the
 * label's credit takes in their own (docs/domain/authors.md#label).
 */
public record LabelAdoptedByAuthor(LabelId label, AuthorId author) implements DomainEvent {

    public LabelAdoptedByAuthor {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(author, "author");
    }
}
