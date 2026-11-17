package com.archive.backend.author.domain;

import com.archive.backend.label.domain.LabelId;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.Objects;

/**
 * A new person replaces the label under its name: the label's credits, memberships and remembered origin become the
 * person's, then the label goes (docs/domain/authors.md#label).
 */
public record LabelReplacedByAuthor(LabelId label, AuthorId author) implements DomainEvent {

    public LabelReplacedByAuthor {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(author, "author");
    }
}
