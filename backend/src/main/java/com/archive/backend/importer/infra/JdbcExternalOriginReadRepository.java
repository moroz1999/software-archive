package com.archive.backend.importer.infra;

import com.archive.backend.importer.read.ExternalOriginReadRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcExternalOriginReadRepository implements ExternalOriginReadRepository {

    private final JdbcClient jdbc;

    @Override
    public Optional<UUID> findEntity(String origin, String kind, String externalId) {
        return jdbc.sql("""
                        SELECT entity_id FROM external_origin
                        WHERE origin = :origin AND kind = :kind AND external_id = :externalId
                        """)
                .param("origin", origin)
                .param("kind", kind)
                .param("externalId", externalId)
                .query(UUID.class)
                .optional();
    }
}
