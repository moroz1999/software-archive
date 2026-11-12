package com.archive.backend.author.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

@ValueObject
public record PersonReference(PersonReferenceType type, PersonReferenceId id) {}
