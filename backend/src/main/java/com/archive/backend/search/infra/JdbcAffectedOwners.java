package com.archive.backend.search.infra;

import com.archive.backend.search.domain.AffectedOwners;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcAffectedOwners implements AffectedOwners {

    private final JdbcClient jdbc;

    @Override
    public List<UUID> peopleCreditedOn(UUID software) {
        return jdbc.sql("""
                        SELECT DISTINCT l.person_id FROM software_contribution sc JOIN label l ON l.id = sc.label_id
                        WHERE sc.software_id = :software AND l.person_id IS NOT NULL
                        """).param("software", software).query(UUID.class).list();
    }

    @Override
    public List<UUID> groupsCreditedOn(UUID software) {
        return jdbc.sql("""
                        SELECT DISTINCT l.group_id FROM software_contribution sc JOIN label l ON l.id = sc.label_id
                        WHERE sc.software_id = :software AND l.group_id IS NOT NULL
                        """).param("software", software).query(UUID.class).list();
    }
}
