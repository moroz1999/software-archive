package com.archive.backend.vote.infra;

import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.vote.domain.SoftwareRating;
import com.archive.backend.vote.domain.SoftwareVotes;
import com.archive.backend.vote.domain.SoftwareVotesRepository;
import com.archive.backend.vote.domain.Vote;
import com.archive.backend.vote.domain.VoteValue;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * The votes of software and its rating, in tables of their own beside the software. The rating row is the root of the
 * aggregate: loading the votes locks it until the transaction ends, so votes on the same software are counted one after
 * another (docs/backend.md#repositories).
 */
@Repository
@RequiredArgsConstructor
public class JdbcSoftwareVotesRepository implements SoftwareVotesRepository {

    private final JdbcClient jdbc;

    @Override
    public Optional<SoftwareVotes> findBySoftware(SoftwareId software) {
        UUID id = software.value();
        // The software itself is locked against deletion (FOR SHARE), then the rating row it may not have yet is made.
        boolean exists = !jdbc.sql("SELECT id FROM software WHERE id = :id FOR SHARE")
                .param("id", id)
                .query(UUID.class)
                .list()
                .isEmpty();
        if (!exists) {
            return Optional.empty();
        }
        jdbc.sql("""
                        INSERT INTO software_rating (software_id, rating, votes_amount) VALUES (:id, 0, 0)
                        ON CONFLICT (software_id) DO NOTHING""").param("id", id).update();
        SoftwareRating rating = jdbc.sql(
                        "SELECT rating, votes_amount FROM software_rating WHERE software_id = :id FOR UPDATE")
                .param("id", id)
                .query((rs, row) -> new SoftwareRating(rs.getDouble("rating"), rs.getInt("votes_amount")))
                .single();
        List<Vote> votes = jdbc.sql("SELECT user_id, value, cast_at FROM software_vote WHERE software_id = :id")
                .param("id", id)
                .query((rs, row) -> new Vote(
                        new UserId(rs.getObject("user_id", UUID.class)),
                        new VoteValue(rs.getInt("value")),
                        rs.getTimestamp("cast_at").toInstant()))
                .list();
        return Optional.of(SoftwareVotes.restore(software, votes, rating));
    }

    @Override
    public void save(SoftwareVotes votes) {
        UUID id = votes.software().value();
        for (Vote vote : votes.votes()) {
            jdbc.sql("""
                            INSERT INTO software_vote (software_id, user_id, value, cast_at)
                            VALUES (:software, :user, :value, :castAt)
                            ON CONFLICT (software_id, user_id)
                            DO UPDATE SET value = EXCLUDED.value, cast_at = EXCLUDED.cast_at""")
                    .param("software", id)
                    .param("user", vote.voter().value())
                    .param("value", vote.value().value())
                    .param("castAt", Timestamp.from(vote.castAt()))
                    .update();
        }
        jdbc.sql("UPDATE software_rating SET rating = :rating, votes_amount = :amount WHERE software_id = :id")
                .param("rating", votes.rating().rating())
                .param("amount", votes.rating().votesAmount())
                .param("id", id)
                .update();
    }
}
