package com.archive.backend.label.app;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.user.domain.Actor;

/** Merges a label found out to be a duplicate of a person into them (docs/domain/authors.md#label). */
public record MergeLabelIntoAuthorCommand(Actor actor, LabelId label, AuthorId author) {}
