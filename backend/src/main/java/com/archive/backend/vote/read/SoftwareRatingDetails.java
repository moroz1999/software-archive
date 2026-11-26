package com.archive.backend.vote.read;

import org.jspecify.annotations.Nullable;

/** The rating of software, the number of votes behind it and the vote of whoever reads it; none if they have none. */
public record SoftwareRatingDetails(
        double rating, int votesAmount, @Nullable Integer myVote) {}
