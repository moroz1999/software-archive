package com.archive.backend.group.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;

/** A group was deleted; told before the deletion, beside GroupChanged. */
public record GroupDeleted(GroupId group) implements DomainEvent {}
