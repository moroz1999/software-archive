package com.archive.backend.vote.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import org.jspecify.annotations.Nullable;

/** The rating of software after a vote, and the vote of the voter; none once withdrawn. */
@ValueObject
public record VoteOutcome(SoftwareRating rating, @Nullable VoteValue mine) {}
