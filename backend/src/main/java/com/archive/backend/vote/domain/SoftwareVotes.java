package com.archive.backend.vote.domain;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.UserId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** The votes cast on one software and the rating they give it (docs/domain/voting.md). */
@AggregateRoot
public final class SoftwareVotes {

    private final SoftwareId software;
    private final List<Vote> votes;
    private SoftwareRating rating;
    private final List<DomainEvent> events = new ArrayList<>();

    private SoftwareVotes(SoftwareId software, List<Vote> votes, SoftwareRating rating) {
        this.software = software;
        this.votes = new ArrayList<>(votes);
        this.rating = rating;
    }

    /** The votes as stored, with the rating they gave. */
    public static SoftwareVotes restore(SoftwareId software, List<Vote> votes, SoftwareRating rating) {
        return new SoftwareVotes(software, votes, rating);
    }

    /**
     * The voter's vote replaces their earlier one, a withdrawn vote being kept as such; the rating is made again by the
     * rule.
     */
    public VoteOutcome cast(UserId voter, VoteValue value, Instant at, RatingRule rule) {
        votes.removeIf(vote -> vote.voter().equals(voter));
        votes.add(new Vote(voter, value, at));
        rating = rule.rate(votes.stream().map(Vote::value).toList());
        events.add(new SoftwareRated(software));
        return new VoteOutcome(rating, value.isWithdrawn() ? null : value);
    }

    public SoftwareId software() {
        return software;
    }

    /** Every vote, withdrawn ones too. */
    public List<Vote> votes() {
        return List.copyOf(votes);
    }

    public SoftwareRating rating() {
        return rating;
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }
}
