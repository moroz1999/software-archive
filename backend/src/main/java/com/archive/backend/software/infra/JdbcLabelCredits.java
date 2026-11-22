package com.archive.backend.software.infra;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.software.domain.LabelCredits;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcLabelCredits implements LabelCredits {

    private final JdbcClient jdbc;

    @Override
    public void transfer(LabelId label, AuthorId author) {
        jdbc.sql("UPDATE software_contribution SET person_id = :author, label_id = NULL WHERE label_id = :label")
                .param("author", author.value())
                .param("label", label.value())
                .update();
    }

    /**
     * Software crediting the group already keeps that one credit, with the roles of both: an unknown role goes once a
     * known one is there.
     */
    @Override
    public void transfer(LabelId label, GroupId group) {
        jdbc.sql("""
                        UPDATE software_contribution g
                        SET role_codes = ARRAY(
                                SELECT DISTINCT r FROM unnest(g.role_codes || l.role_codes) AS r
                                WHERE r <> 'unknown'
                                   OR NOT EXISTS (SELECT 1 FROM unnest(g.role_codes || l.role_codes) AS k
                                                  WHERE k <> 'unknown')),
                            notes = COALESCE(g.notes, l.notes)
                        FROM software_contribution l
                        WHERE l.label_id = :label AND g.group_id = :group AND g.software_id = l.software_id
                        """)
                .param("group", group.value())
                .param("label", label.value())
                .update();
        jdbc.sql("""
                        DELETE FROM software_contribution l
                        WHERE l.label_id = :label
                          AND EXISTS (SELECT 1 FROM software_contribution g
                                      WHERE g.group_id = :group AND g.software_id = l.software_id)
                        """)
                .param("group", group.value())
                .param("label", label.value())
                .update();
        jdbc.sql("UPDATE software_contribution SET group_id = :group, label_id = NULL WHERE label_id = :label")
                .param("group", group.value())
                .param("label", label.value())
                .update();
    }
}
