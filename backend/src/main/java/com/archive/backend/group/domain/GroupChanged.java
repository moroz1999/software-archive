package com.archive.backend.group.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;

/** A group was added, edited or deleted. */
public record GroupChanged(GroupId group) implements DomainEvent {}
