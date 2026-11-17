package com.archive.backend.label.app;

import com.archive.backend.label.domain.LabelId;
import com.archive.backend.user.domain.Actor;

/** Replaces a label with a new person of the same name (docs/domain/authors.md#label). */
public record MakeAuthorOfLabelCommand(Actor actor, LabelId label) {}
