package com.archive.backend.group.infra;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.LabelMemberships;
import com.archive.backend.label.domain.LabelId;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcLabelMemberships implements LabelMemberships {

    private final JdbcClient jdbc;

    @Override
    public void transfer(LabelId label, AuthorId author) {
        jdbc.sql("UPDATE group_member SET person_id = :author, label_id = NULL WHERE label_id = :label")
                .param("author", author.value())
                .param("label", label.value())
                .update();
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
}
