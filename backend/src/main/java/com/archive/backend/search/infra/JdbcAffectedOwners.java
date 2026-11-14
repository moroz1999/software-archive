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
        return creditedOn(software, "person_id");
    }

    @Override
    public List<UUID> groupsCreditedOn(UUID software) {
        return creditedOn(software, "group_id");
    }

    /** The owners credited by themselves or by a label, by the owner column of the credits and of the labels. */
    private List<UUID> creditedOn(UUID software, String ownerColumn) {
        return jdbc.sql("""
                        SELECT DISTINCT COALESCE(sc.%1$s, l.%1$s) FROM software_contribution sc
                        LEFT JOIN label l ON l.id = sc.label_id
                        WHERE sc.software_id = :software AND COALESCE(sc.%1$s, l.%1$s) IS NOT NULL
                        """.formatted(ownerColumn))
                .param("software", software)
                .query(UUID.class)
                .list();
    }
}
