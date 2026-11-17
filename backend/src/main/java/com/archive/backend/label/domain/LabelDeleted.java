package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;

/** A label is being deleted with its credits and memberships; told while they are still there. */
public record LabelDeleted(LabelId label) implements DomainEvent {}
