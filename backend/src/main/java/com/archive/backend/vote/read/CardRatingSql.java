package com.archive.backend.vote.read;

/**
 * The rating of software and the number of votes behind it as the columns {@code rating} and {@code votes_amount}, 0 and
 * 0 for software nobody voted on: what every card of software shows (docs/domain/voting.md).
 */
public final class CardRatingSql {

    private CardRatingSql() {}

    /** The two columns for the software of the alias. */
    public static String of(String software) {
        return "COALESCE((SELECT r.rating FROM software_rating r WHERE r.software_id = " + software + ".id), 0)"
                + " AS rating, COALESCE((SELECT r.votes_amount FROM software_rating r WHERE r.software_id = "
                + software + ".id), 0) AS votes_amount";
    }
}
