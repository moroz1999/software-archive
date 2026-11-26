package com.archive.backend.vote.domain;

import java.util.List;

/**
 * How the votes of a work make its rating, as on ZX-Art (docs/domain/voting.md#rating): votes farther than
 * {@value #ANOMALY_THRESHOLD} standard deviations from the mean are dropped, and the average of the rest is weighted by
 * {@value #OBJECTIVE_VOTES} votes of the archive's average.
 */
public class RatingRule {

    /** How many standard deviations from the mean a vote may lie and still count. */
    static final double ANOMALY_THRESHOLD = 1.96;

    /** How many votes of the archive's average every rating holds besides its own. */
    static final int OBJECTIVE_VOTES = 10;

    private final AverageVote average;

    public RatingRule(AverageVote average) {
        this.average = average;
    }

    /** The rating the votes give; withdrawn votes count for nothing. */
    public SoftwareRating rate(List<VoteValue> votes) {
        List<Integer> counted = withoutAnomalies(votes.stream()
                .filter(vote -> !vote.isWithdrawn())
                .map(VoteValue::value)
                .toList());
        if (counted.isEmpty()) {
            return SoftwareRating.NONE;
        }
        double sum = counted.stream().mapToInt(Integer::intValue).sum();
        return new SoftwareRating(
                (sum + average.value() * OBJECTIVE_VOTES) / (counted.size() + OBJECTIVE_VOTES), counted.size());
    }

    /** The votes left once those too far from the others are dropped: none of a single vote or of equal ones. */
    private static List<Integer> withoutAnomalies(List<Integer> votes) {
        if (votes.size() <= 1) {
            return votes;
        }
        double mean = votes.stream().mapToInt(Integer::intValue).average().orElseThrow();
        double squares = votes.stream()
                .mapToDouble(vote -> (vote - mean) * (vote - mean))
                .sum();
        double deviation = Math.sqrt(squares / (votes.size() - 1));
        if (deviation == 0) {
            return votes;
        }
        return votes.stream()
                .filter(vote -> Math.abs((vote - mean) / deviation) <= ANOMALY_THRESHOLD)
                .toList();
    }
}
