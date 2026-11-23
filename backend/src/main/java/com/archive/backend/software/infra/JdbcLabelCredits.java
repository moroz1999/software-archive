package com.archive.backend.software.infra;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.software.domain.LabelCredits;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcLabelCredits implements LabelCredits {

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

    /**
     * Software already crediting the participant keeps that one credit, with the roles of both: an unknown role goes
     * once a known one is there; the other credits move to the participant. {@code column} is a constant of this class.
     */
    private void transfer(LabelId label, String column, UUID participant) {
        jdbc.sql("""
                        UPDATE software_contribution g
                        SET role_codes = ARRAY(
                                SELECT DISTINCT r FROM unnest(g.role_codes || l.role_codes) AS r
                                WHERE r <> 'unknown'
                                   OR NOT EXISTS (SELECT 1 FROM unnest(g.role_codes || l.role_codes) AS k
                                                  WHERE k <> 'unknown')),
                            notes = COALESCE(g.notes, l.notes)
                        FROM software_contribution l
                        WHERE l.label_id = :label AND g.%1$s = :participant AND g.software_id = l.software_id
                        """.formatted(column))
                .param("participant", participant)
                .param("label", label.value())
                .update();
        jdbc.sql("""
                        DELETE FROM software_contribution l
                        WHERE l.label_id = :label
                          AND EXISTS (SELECT 1 FROM software_contribution g
                                      WHERE g.%1$s = :participant AND g.software_id = l.software_id)
                        """.formatted(column))
                .param("participant", participant)
                .param("label", label.value())
                .update();
        String moved =
                column.equals("label_id") ? "label_id = :participant" : column + " = :participant, label_id = NULL";
        jdbc.sql("UPDATE software_contribution SET " + moved + " WHERE label_id = :label")
                .param("participant", participant)
                .param("label", label.value())
                .update();
    }
}
