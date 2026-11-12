package com.archive.backend.author.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;

/** A person was added, edited or deleted. */
public record AuthorChanged(AuthorId author) implements DomainEvent {}
