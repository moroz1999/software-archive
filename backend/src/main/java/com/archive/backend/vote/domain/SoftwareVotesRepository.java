package com.archive.backend.vote.domain;

import com.archive.backend.software.domain.SoftwareId;
import java.util.Optional;

/** The votes of each software; none of software that does not exist. */
public interface SoftwareVotesRepository {

    Optional<SoftwareVotes> findBySoftware(SoftwareId software);

    void save(SoftwareVotes votes);
}
