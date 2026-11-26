package com.archive.backend.vote.app;

import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import java.util.List;

/** The votes the reader cast on the software of one page of a list (docs/features/voting.md#rest). */
public record ListMyVotesQuery(Actor actor, List<SoftwareId> software) {

    public ListMyVotesQuery {
        software = List.copyOf(software);
    }
}
