package com.archive.backend.label.app;

import com.archive.backend.label.domain.LabelId;
import com.archive.backend.user.domain.Actor;

/** Makes a new person known by a label (docs/domain/authors.md#label). */
public record MakeAuthorOfLabelCommand(Actor actor, LabelId label) {}
