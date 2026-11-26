package com.archive.backend.vote.app;

import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.vote.domain.SoftwareRating;
import com.archive.backend.vote.domain.SoftwareVotes;
import com.archive.backend.vote.domain.SoftwareVotesRepository;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** The votes of the software it is told exists. */
class InMemorySoftwareVotesRepository implements SoftwareVotesRepository {

    private final Set<SoftwareId> software = new HashSet<>();
    private final Map<SoftwareId, SoftwareVotes> saved = new LinkedHashMap<>();

    void addSoftware(SoftwareId id) {
        software.add(id);
    }

    Optional<SoftwareVotes> saved(SoftwareId id) {
        return Optional.ofNullable(saved.get(id));
    }

    @Override
    public Optional<SoftwareVotes> findBySoftware(SoftwareId id) {
        if (!software.contains(id)) {
            return Optional.empty();
        }
        SoftwareVotes stored = saved.get(id);
        return Optional.of(
                stored == null
                        ? SoftwareVotes.restore(id, List.of(), SoftwareRating.NONE)
                        : SoftwareVotes.restore(id, stored.votes(), stored.rating()));
    }

    @Override
    public void save(SoftwareVotes votes) {
        saved.put(votes.software(), votes);
    }
}
