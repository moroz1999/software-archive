package com.archive.backend.label.app;

import com.archive.backend.label.domain.LabelId;
import com.archive.backend.user.domain.Actor;

/** Deletes a label with its credits and memberships (docs/domain/authors.md#editing-a-label). */
public record DeleteLabelCommand(Actor actor, LabelId id) {}
