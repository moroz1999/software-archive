package com.archive.backend.vote.infra;

import com.archive.backend.vote.read.MyVote;
import com.archive.backend.vote.read.SoftwareRatingDetails;
import com.archive.backend.vote.read.SoftwareVoteEntry;
import com.archive.backend.vote.read.SoftwareVoteReadRepository;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcSoftwareVoteReadRepository implements SoftwareVoteReadRepository {

    private final JdbcClient jdbc;

    @Override
    public List<MyVote> votesBy(UUID user, List<UUID> software) {
        return jdbc.sql("""
                        SELECT software_id, value FROM software_vote
                        WHERE user_id = :user AND software_id IN (:software) AND value > 0""")
                .param("user", user)
                .param("software", software)
                .query((rs, row) -> new MyVote(rs.getObject("software_id", UUID.class), rs.getInt("value")))
                .list();
    }

    @Override
    public Optional<SoftwareRatingDetails> ratingOf(UUID software, @Nullable UUID user) {
        return jdbc.sql("""
                        SELECT COALESCE(r.rating, 0) AS rating, COALESCE(r.votes_amount, 0) AS votes_amount,
                               (SELECT v.value FROM software_vote v
                                WHERE v.software_id = s.id AND v.user_id = :user AND v.value > 0) AS my_vote
                        FROM software s
                        LEFT JOIN software_rating r ON r.software_id = s.id
                        WHERE s.id = :software""")
                .param("software", software)
                .param("user", user, Types.OTHER)
                .query((rs, row) -> new SoftwareRatingDetails(
                        rs.getDouble("rating"), rs.getInt("votes_amount"), rs.getObject("my_vote", Integer.class)))
                .optional();
    }

    @Override
    public Optional<List<SoftwareVoteEntry>> votesOf(UUID software) {
        boolean exists = jdbc.sql("SELECT EXISTS (SELECT 1 FROM software WHERE id = :id)")
                .param("id", software)
                .query(Boolean.class)
                .single();
        if (!exists) {
            return Optional.empty();
        }
        return Optional.of(jdbc.sql("""
                        SELECT u.name, v.value, v.cast_at
                        FROM software_vote v
                        JOIN app_user u ON u.id = v.user_id
                        WHERE v.software_id = :id AND v.value > 0
                        ORDER BY v.cast_at DESC, u.name""")
                .param("id", software)
                .query((rs, row) -> new SoftwareVoteEntry(
                        rs.getString("name"),
                        rs.getInt("value"),
                        rs.getTimestamp("cast_at").toInstant()))
                .list());
    }
}
