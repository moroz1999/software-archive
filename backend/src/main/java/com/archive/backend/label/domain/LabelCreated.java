package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;

/** A label of nobody was added. */
public record LabelCreated(LabelId label) implements DomainEvent {}
