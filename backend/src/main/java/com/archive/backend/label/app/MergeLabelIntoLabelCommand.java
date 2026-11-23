package com.archive.backend.label.app;

import com.archive.backend.label.domain.LabelId;
import com.archive.backend.user.domain.Actor;

/** Merges a label found out to be a duplicate of another label into it (docs/domain/authors.md#label). */
public record MergeLabelIntoLabelCommand(Actor actor, LabelId label, LabelId into) {}
