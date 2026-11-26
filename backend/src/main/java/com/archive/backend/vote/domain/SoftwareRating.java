package com.archive.backend.vote.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** The rating of software and the number of votes it stands on (docs/domain/voting.md#rating). */
@ValueObject
public record SoftwareRating(double rating, int votesAmount) {

    /** The rating of software nobody voted on. */
    public static final SoftwareRating NONE = new SoftwareRating(0, 0);
}
