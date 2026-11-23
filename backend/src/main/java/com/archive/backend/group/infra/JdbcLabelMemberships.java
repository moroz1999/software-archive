package com.archive.backend.group.infra;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.LabelMemberships;
import com.archive.backend.label.domain.LabelId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcLabelMemberships implements LabelMemberships {

    private final JdbcClient jdbc;

    @Override
    public void transfer(LabelId label, AuthorId author) {
        transfer(label, "person_id", author.value());
    }

    @Override
    public void transfer(LabelId label, GroupId group) {
        jdbc.sql("""
                        INSERT INTO group_member_group (group_id, member_group_id)
                        SELECT DISTINCT group_id, :group FROM group_member WHERE label_id = :label AND group_id <> :group
                        ON CONFLICT DO NOTHING
                        """)
                .param("group", group.value())
                .param("label", label.value())
                .update();
        jdbc.sql("DELETE FROM group_member WHERE label_id = :label")
                .param("label", label.value())
                .update();
    }

    @Override
    public void transfer(LabelId label, LabelId into) {
        transfer(label, "label_id", into.value());
    }

    /**
     * A group the member already has keeps that one membership, with the roles of both: an unknown role goes once a
     * known one is there; the other memberships move to the member. {@code column} is a constant of this class.
     */
    private void transfer(LabelId label, String column, UUID member) {
        jdbc.sql("""
                        UPDATE group_member g
                        SET role_codes = ARRAY(
                                SELECT DISTINCT r FROM unnest(g.role_codes || l.role_codes) AS r
                                WHERE r <> 'unknown'
                                   OR NOT EXISTS (SELECT 1 FROM unnest(g.role_codes || l.role_codes) AS k
                                                  WHERE k <> 'unknown'))
                        FROM group_member l
                        WHERE l.label_id = :label AND g.%1$s = :member AND g.group_id = l.group_id
                        """.formatted(column))
                .param("member", member)
                .param("label", label.value())
                .update();
        jdbc.sql("""
                        DELETE FROM group_member l
                        WHERE l.label_id = :label
                          AND EXISTS (SELECT 1 FROM group_member g
                                      WHERE g.%1$s = :member AND g.group_id = l.group_id)
                        """.formatted(column))
                .param("member", member)
                .param("label", label.value())
                .update();
        String moved = column.equals("label_id") ? "label_id = :member" : column + " = :member, label_id = NULL";
        jdbc.sql("UPDATE group_member SET " + moved + " WHERE label_id = :label")
                .param("member", member)
                .param("label", label.value())
                .update();
    }
}
