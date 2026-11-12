package com.archive.backend.author.infra;

import com.archive.backend.author.domain.PersonReference;
import com.archive.backend.author.domain.PersonReferenceCatalog;
import com.archive.backend.author.domain.PersonReferenceType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcPersonReferenceCatalog implements PersonReferenceCatalog {

    private final JdbcClient jdbc;

    @Override
    public boolean contains(PersonReference reference) {
        String sql = reference.type() == PersonReferenceType.AUTHOR
                ? "SELECT EXISTS (SELECT 1 FROM person WHERE id = :id)"
                : "SELECT EXISTS (SELECT 1 FROM label WHERE id = :id)";
        return jdbc.sql(sql)
                .param("id", reference.id().value())
                .query(Boolean.class)
                .single();
    }
}
