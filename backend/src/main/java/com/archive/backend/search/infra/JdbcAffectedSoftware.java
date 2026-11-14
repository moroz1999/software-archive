package com.archive.backend.search.infra;

import com.archive.backend.search.domain.AffectedSoftware;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcAffectedSoftware implements AffectedSoftware {

    private final JdbcClient jdbc;

    @Override
    public List<UUID> inSubtreeOf(UUID category) {
        return jdbc.sql("""
                        WITH RECURSIVE subtree (id) AS (
                            SELECT CAST(:category AS uuid)
                            UNION
                            SELECT c.id FROM category c JOIN subtree s ON c.parent_id = s.id)
                        SELECT DISTINCT sc.software_id FROM software_category sc JOIN subtree s ON s.id = sc.category_id
                        """).param("category", category).query(UUID.class).list();
    }

    @Override
    public List<UUID> creditedOn(Collection<UUID> labels) {
        return jdbc.sql("""
                        SELECT software_id FROM software_contribution WHERE label_id IN (:labels)
                        UNION
                        SELECT software_id FROM software_developer_label WHERE label_id IN (:labels)
                        """).param("labels", labels).query(UUID.class).list();
    }

    @Override
    public List<UUID> creditingPerson(UUID person) {
        return jdbc.sql("SELECT DISTINCT software_id FROM software_contribution WHERE person_id = :person")
                .param("person", person)
                .query(UUID.class)
                .list();
    }

    @Override
    public List<UUID> developedByGroup(UUID group) {
        return jdbc.sql("SELECT software_id FROM software_developer_group WHERE group_id = :group")
                .param("group", group)
                .query(UUID.class)
                .list();
    }
}
