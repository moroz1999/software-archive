package com.archive.backend.author.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.UUID;

@ValueObject
public record PersonReferenceId(UUID value) {}
