package com.archive.backend.vote.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** The average vote of the whole archive, which every rating leans towards (docs/domain/voting.md). */
@ValueObject
public record AverageVote(double value) {}
