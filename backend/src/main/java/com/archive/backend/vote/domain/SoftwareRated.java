package com.archive.backend.vote.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;
import com.archive.backend.software.domain.SoftwareId;

/** The rating of software changed. */
public record SoftwareRated(SoftwareId software) implements DomainEvent {}
