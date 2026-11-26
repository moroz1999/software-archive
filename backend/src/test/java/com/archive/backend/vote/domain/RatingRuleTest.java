package com.archive.backend.vote.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/** The rating of ZX-Art: anomalies dropped, then the average weighted by the archive's (docs/domain/voting.md#rating). */
class RatingRuleTest {

    private final RatingRule rule = new RatingRule(new AverageVote(3.8));

    private static List<VoteValue> votes(int... stars) {
        return IntStream.of(stars).mapToObj(VoteValue::new).toList();
    }

    @Test
    void softwareNobodyVotedOnHasNoRating() {
        assertThat(rule.rate(List.of())).isEqualTo(SoftwareRating.NONE);
    }

    @Test
    void aFewVotesLeanTowardsTheAverageOfTheArchive() {
        SoftwareRating rating = rule.rate(votes(5, 5, 5));

        // (5 × 3 + 3.8 × 10) / (3 + 10)
        assertThat(rating.rating()).isCloseTo(53.0 / 13, within(1e-9));
        assertThat(rating.votesAmount()).isEqualTo(3);
    }

    @Test
    void manyVotesOutweighTheAverageOfTheArchive() {
        List<VoteValue> votes = new ArrayList<>();
        votes.addAll(Collections.nCopies(100, new VoteValue(5)));
        votes.addAll(Collections.nCopies(100, new VoteValue(4)));

        SoftwareRating rating = rule.rate(votes);

        // (4.5 × 200 + 3.8 × 10) / (200 + 10)
        assertThat(rating.rating()).isCloseTo(938.0 / 210, within(1e-9));
        assertThat(rating.votesAmount()).isEqualTo(200);
    }

    @Test
    void aVoteFarFromTheOthersIsDropped() {
        // Mean 4.6, deviation 1.26: the one is 2.85 deviations away.
        SoftwareRating rating = rule.rate(votes(5, 5, 5, 5, 5, 5, 5, 5, 5, 1));

        assertThat(rating.votesAmount()).isEqualTo(9);
        assertThat(rating.rating()).isCloseTo((5.0 * 9 + 38) / 19, within(1e-9));
    }

    @Test
    void aVoteWithinTheSpreadOfTheOthersCounts() {
        // Mean 4.2, deviation 1.79: the one is 1.79 deviations away.
        SoftwareRating rating = rule.rate(votes(5, 5, 5, 5, 1));

        assertThat(rating.votesAmount()).isEqualTo(5);
        assertThat(rating.rating()).isCloseTo((21.0 + 38) / 15, within(1e-9));
    }

    @Test
    void aSingleVoteIsNeverDropped() {
        SoftwareRating rating = rule.rate(votes(1));

        assertThat(rating.votesAmount()).isEqualTo(1);
        assertThat(rating.rating()).isCloseTo((1.0 + 38) / 11, within(1e-9));
    }

    @Test
    void equalVotesAreNeverDropped() {
        assertThat(rule.rate(votes(2, 2, 2, 2)).votesAmount()).isEqualTo(4);
    }

    @Test
    void withdrawnVotesCountForNothing() {
        assertThat(rule.rate(votes(5, 0, 0))).isEqualTo(rule.rate(votes(5)));
        assertThat(rule.rate(votes(0))).isEqualTo(SoftwareRating.NONE);
    }
}
