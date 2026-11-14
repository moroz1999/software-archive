package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;

/** Software was deleted; told before the deletion, beside SoftwareChanged. */
public record SoftwareDeleted(SoftwareId software) implements DomainEvent {}
