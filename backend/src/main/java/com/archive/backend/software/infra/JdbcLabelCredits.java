package com.archive.backend.software.infra;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.software.domain.LabelCredits;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcLabelCredits implements LabelCredits {

    /** The credits of software and of releases: the table and the column of what they credit. */
    private record Credits(String table, String of, String notes) {}

    private static final List<Credits> CREDITS = List.of(
            new Credits("software_contribution", "software_id", ", notes = COALESCE(kept.notes, gone.notes)"),
            new Credits("software_release_contribution", "release_id", ""));

    /** The roles of both credits, each once; an unknown role goes once a known one is there. */
    private static final String JOINED_ROLES = """
            role_codes = ARRAY(
                    SELECT DISTINCT r FROM unnest(kept.role_codes || gone.role_codes) AS r
                    WHERE r <> 'unknown'
                       OR NOT EXISTS (SELECT 1 FROM unnest(kept.role_codes || gone.role_codes) AS k
                                      WHERE k <> 'unknown'))
            """;

    private final JdbcClient jdbc;

    @Override
    public void transfer(LabelId label, AuthorId author) {
        transfer(label, "person_id", author.value());
    }

    @Override
    public void transfer(LabelId label, GroupId group) {
        transfer(label, "group_id", group.value());
    }

    @Override
    public void transfer(LabelId label, LabelId into) {
        transfer(label, "label_id", into.value());
    }

    @Override
    public void takeIn(LabelId label, AuthorId owner) {
        CREDITS.forEach(credits -> join(credits, "label_id", label.value(), "person_id", owner.value()));
    }

    @Override
    public void takeIn(LabelId label, GroupId owner) {
        CREDITS.forEach(credits -> join(credits, "label_id", label.value(), "group_id", owner.value()));
    }

    /**
     * Software or a release already crediting the participant keeps that one credit, with the roles of both; the other
     * credits move to the participant. {@code column} is a constant of this class.
     */
    private void transfer(LabelId label, String column, UUID participant) {
        CREDITS.forEach(credits -> {
            join(credits, column, participant, "label_id", label.value());
            String moved =
                    column.equals("label_id") ? "label_id = :participant" : column + " = :participant, label_id = NULL";
            jdbc.sql("UPDATE " + credits.table() + " SET " + moved + " WHERE label_id = :label")
                    .param("participant", participant)
                    .param("label", label.value())
                    .update();
        });
    }

    /**
     * Where the same software or release credits both, the credit of {@code keptColumn} takes in the roles of the credit
     * of {@code goneColumn}, which goes. The columns are constants of this class.
     */
    private void join(Credits credits, String keptColumn, UUID kept, String goneColumn, UUID gone) {
        jdbc.sql("""
                        UPDATE %1$s kept
                        SET %2$s%3$s
                        FROM %1$s gone
                        WHERE kept.%4$s = :kept AND gone.%5$s = :gone AND kept.%6$s = gone.%6$s
                        """.formatted(credits.table(), JOINED_ROLES, credits.notes(), keptColumn, goneColumn, credits.of()))
                .param("kept", kept)
                .param("gone", gone)
                .update();
        jdbc.sql("""
                        DELETE FROM %1$s gone
                        WHERE gone.%3$s = :gone
                          AND EXISTS (SELECT 1 FROM %1$s kept WHERE kept.%2$s = :kept AND kept.%4$s = gone.%4$s)
                        """.formatted(credits.table(), keptColumn, goneColumn, credits.of()))
                .param("kept", kept)
                .param("gone", gone)
                .update();
    }
}
