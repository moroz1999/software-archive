package com.archive.backend.vote.read;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The ratings and the votes of software as visitors see them; nothing of software that does not exist. */
public interface SoftwareVoteReadRepository {

    /** The rating of the software and the vote the user cast on it; no user is a public visitor. */
    Optional<SoftwareRatingDetails> ratingOf(UUID software, @Nullable UUID user);

    /** The votes standing on the software, newest first; withdrawn ones are not among them. */
    Optional<List<SoftwareVoteEntry>> votesOf(UUID software);

    /** The votes the user has standing on any of the software; withdrawn ones are not among them. */
    List<MyVote> votesBy(UUID user, List<UUID> software);
}
