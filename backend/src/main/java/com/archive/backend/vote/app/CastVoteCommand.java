package com.archive.backend.vote.app;

import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.vote.domain.VoteValue;

/** A user votes on software, or withdraws their vote with {@link VoteValue#WITHDRAWN}. */
public record CastVoteCommand(Actor actor, SoftwareId software, VoteValue value) {}
