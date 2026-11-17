package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;

/** A label was renamed or relocated. */
public record LabelChanged(LabelId label) implements DomainEvent {}
