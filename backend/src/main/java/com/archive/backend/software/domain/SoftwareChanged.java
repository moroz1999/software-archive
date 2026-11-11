package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;

/** Software was added, edited or deleted. */
public record SoftwareChanged(SoftwareId software) implements DomainEvent {}
