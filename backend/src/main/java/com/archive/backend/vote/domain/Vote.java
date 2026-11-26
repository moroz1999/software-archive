package com.archive.backend.vote.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.user.domain.UserId;
import java.time.Instant;

/** The vote of one user on one software, as they last cast it. */
@ValueObject
public record Vote(UserId voter, VoteValue value, Instant castAt) {}
