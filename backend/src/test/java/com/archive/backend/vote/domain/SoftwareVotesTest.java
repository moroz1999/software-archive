package com.archive.backend.vote.domain;

import static com.archive.backend.user.TestActors.MEMBER_ID;
import static com.archive.backend.user.TestActors.OTHER_ID;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.software.domain.SoftwareId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SoftwareVotesTest {

    private static final SoftwareId ELITE = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final Instant MONDAY = Instant.parse("2026-11-23T10:00:00Z");
    private static final Instant TUESDAY = Instant.parse("2026-11-24T10:00:00Z");

    private final RatingRule rule = new RatingRule(new AverageVote(3.8));
    private final SoftwareVotes votes = SoftwareVotes.restore(ELITE, List.of(), SoftwareRating.NONE);

    @Test
    void aVoteGivesTheSoftwareItsRating() {
        VoteOutcome outcome = votes.cast(MEMBER_ID, new VoteValue(5), MONDAY, rule);

        SoftwareRating expected = rule.rate(List.of(new VoteValue(5)));
        assertThat(votes.rating()).isEqualTo(expected);
        assertThat(outcome).isEqualTo(new VoteOutcome(expected, new VoteValue(5)));
        assertThat(votes.votes()).containsExactly(new Vote(MEMBER_ID, new VoteValue(5), MONDAY));
    }

    @Test
    void aNewVoteOfTheSameUserReplacesTheirEarlierOne() {
        votes.cast(MEMBER_ID, new VoteValue(5), MONDAY, rule);
        votes.cast(OTHER_ID, new VoteValue(4), MONDAY, rule);

        votes.cast(MEMBER_ID, new VoteValue(2), TUESDAY, rule);

        assertThat(votes.votes())
                .containsExactlyInAnyOrder(
                        new Vote(MEMBER_ID, new VoteValue(2), TUESDAY), new Vote(OTHER_ID, new VoteValue(4), MONDAY));
        assertThat(votes.rating()).isEqualTo(rule.rate(List.of(new VoteValue(2), new VoteValue(4))));
    }

    @Test
    void aWithdrawnVoteIsKeptButCountsForNothing() {
        votes.cast(MEMBER_ID, new VoteValue(5), MONDAY, rule);
        votes.cast(OTHER_ID, new VoteValue(4), MONDAY, rule);

        VoteOutcome outcome = votes.cast(MEMBER_ID, VoteValue.WITHDRAWN, TUESDAY, rule);

        SoftwareRating expected = rule.rate(List.of(new VoteValue(4)));
        assertThat(outcome).isEqualTo(new VoteOutcome(expected, null));
        assertThat(votes.votes()).contains(new Vote(MEMBER_ID, VoteValue.WITHDRAWN, TUESDAY));
    }

    @Test
    void tellsTheRatingChanged() {
        votes.cast(MEMBER_ID, new VoteValue(5), MONDAY, rule);

        assertThat(votes.pullEvents()).containsExactly(new SoftwareRated(ELITE));
    }
}
