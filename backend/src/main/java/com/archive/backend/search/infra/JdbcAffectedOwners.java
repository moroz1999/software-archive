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

    @Override
    public List<UUID> labelsOfNobodyCreditedOn(UUID software) {
        return jdbc.sql("""
                        SELECT l.id FROM software_contribution sc
                        JOIN label l ON l.id = sc.label_id
                        WHERE sc.software_id = :software AND l.person_id IS NULL AND l.group_id IS NULL
                        """).param("software", software).query(UUID.class).list();
    }

    @Override
    public List<UUID> peopleOwning(UUID label) {
        return owning(label, "person_id");
    }

    @Override
    public List<UUID> groupsOwning(UUID label) {
        return owning(label, "group_id");
    }

    private List<UUID> owning(UUID label, String ownerColumn) {
        return jdbc.sql("SELECT %1$s FROM label WHERE id = :label AND %1$s IS NOT NULL".formatted(ownerColumn))
                .param("label", label)
                .query(UUID.class)
                .list();
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
