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
                        SELECT DISTINCT software_id FROM software_contribution WHERE label_id IN (:labels)
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
    public List<UUID> creditingGroup(UUID group) {
        return jdbc.sql("SELECT DISTINCT software_id FROM software_contribution WHERE group_id = :group")
                .param("group", group)
                .query(UUID.class)
                .list();
    }

    @Override
    public List<UUID> creditedFromCity(UUID city) {
        return jdbc.sql("""
                        SELECT DISTINCT sc.software_id
                        FROM software_contribution sc
                        LEFT JOIN label l ON l.id = sc.label_id
                        JOIN entity_location el
                          ON (el.entity_type = 'PERSON' AND el.entity_id = COALESCE(sc.person_id, l.person_id))
                          OR (el.entity_type = 'GROUP' AND el.entity_id = COALESCE(sc.group_id, l.group_id))
                        WHERE el.city_id = :city
                        """).param("city", city).query(UUID.class).list();
    }

    @Override
    public List<UUID> creditedFromCountry(UUID country) {
        return jdbc.sql("""
                        SELECT DISTINCT sc.software_id
                        FROM software_contribution sc
                        LEFT JOIN label l ON l.id = sc.label_id
                        JOIN entity_location el
                          ON (el.entity_type = 'PERSON' AND el.entity_id = COALESCE(sc.person_id, l.person_id))
                          OR (el.entity_type = 'GROUP' AND el.entity_id = COALESCE(sc.group_id, l.group_id))
                        LEFT JOIN city ON city.id = el.city_id
                        WHERE COALESCE(el.country_id, city.country_id) = :country
                        """).param("country", country).query(UUID.class).list();
    }
}
