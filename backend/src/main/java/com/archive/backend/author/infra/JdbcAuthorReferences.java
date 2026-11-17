package com.archive.backend.author.infra;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorReferences;
import com.archive.backend.label.domain.LabelId;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcAuthorReferences implements AuthorReferences {

    private final JdbcClient jdbc;

    @Override
    public void replaceLabel(LabelId label, AuthorId author) {
        jdbc.sql("""
                        UPDATE external_origin SET kind = 'AUTHOR', entity_id = :author
                        WHERE kind = 'LABEL' AND entity_id = :label
                        """)
                .param("author", author.value())
                .param("label", label.value())
                .update();
        jdbc.sql("UPDATE group_member SET person_id = :author, label_id = NULL WHERE label_id = :label")
                .param("author", author.value())
                .param("label", label.value())
                .update();
        jdbc.sql("UPDATE software_contribution SET person_id = :author, label_id = NULL WHERE label_id = :label")
                .param("author", author.value())
                .param("label", label.value())
                .update();
        jdbc.sql("DELETE FROM label WHERE id = :label")
                .param("label", label.value())
                .update();
    }
}
