package com.archive.backend.software.infra;

import com.archive.backend.software.domain.SoftwareCompilationCatalog;
import com.archive.backend.software.domain.SoftwareId;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcSoftwareCompilationCatalog implements SoftwareCompilationCatalog {

    private final JdbcClient jdbc;

    @Override
    public void lockChanges() {
        // Two edits of different programs must not both validate a link before the other is visible.
        jdbc.sql("SELECT pg_advisory_xact_lock(hashtext('software_compilation_item'))")
                .query((rs, row) -> true)
                .single();
    }

    @Override
    public boolean allExist(Set<SoftwareId> ids) {
        return jdbc.sql("SELECT id FROM software WHERE id IN (:ids) ORDER BY id FOR KEY SHARE")
                        .param("ids", ids.stream().map(SoftwareId::value).toList())
                        .query(UUID.class)
                        .list()
                        .size()
                == ids.size();
    }

    @Override
    public boolean includes(SoftwareId compilation, Set<SoftwareId> programs) {
        return jdbc.sql("""
                        WITH RECURSIVE containing(id) AS (
                            SELECT compilation_id FROM software_compilation_item WHERE item_id = :compilation
                            UNION
                            SELECT i.compilation_id FROM software_compilation_item i JOIN containing c ON c.id = i.item_id
                        )
                        SELECT EXISTS (SELECT 1 FROM containing WHERE id IN (:programs))
                        """)
                .param("compilation", compilation.value())
                .param("programs", programs.stream().map(SoftwareId::value).toList())
                .query(Boolean.class)
                .single();
    }
}
