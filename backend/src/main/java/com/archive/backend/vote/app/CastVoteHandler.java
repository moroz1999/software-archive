package com.archive.backend.vote.app;

import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.vote.domain.RatingRule;
import com.archive.backend.vote.domain.SoftwareVotes;
import com.archive.backend.vote.domain.SoftwareVotesRepository;
import com.archive.backend.vote.domain.VoteOutcome;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CastVoteHandler {

    private final SoftwareVotesRepository repository;
    private final RatingRule rule;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    @Transactional
    public VoteOutcome handle(CastVoteCommand c) {
        UserId voter = c.actor().signedIn();
        SoftwareVotes votes = repository.findBySoftware(c.software()).orElseThrow(SoftwareNotFoundException::new);
        VoteOutcome outcome = votes.cast(voter, c.value(), clock.instant(), rule);
        repository.save(votes);
        votes.pullEvents().forEach(events::publishEvent);
        return outcome;
    }
}
