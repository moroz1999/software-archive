package com.archive.backend.author.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;

/** A person was deleted; told before the deletion, beside AuthorChanged. */
public record AuthorDeleted(AuthorId author) implements DomainEvent {}
