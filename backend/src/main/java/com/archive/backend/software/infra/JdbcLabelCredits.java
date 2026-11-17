package com.archive.backend.software.infra;

import com.archive.backend.author.domain.AuthorId;
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
}
