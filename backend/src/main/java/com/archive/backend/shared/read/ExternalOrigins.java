package com.archive.backend.shared.read;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Reads where imports took an entry from, by the kind the importer remembers it as. */
public final class ExternalOrigins {

    private ExternalOrigins() {}

    /** The origins of the entry of the kind ({@code SOFTWARE}, {@code AUTHOR}, {@code GROUP}), by archive then id. */
    public static List<ExternalOrigin> of(JdbcClient jdbc, String kind, UUID entityId) {
        return jdbc.sql("""
                        SELECT origin, external_id, url FROM external_origin
                        WHERE kind = :kind AND entity_id = :id
                        ORDER BY origin, external_id
                        """)
                .param("kind", kind)
                .param("id", entityId)
                .query((rs, row) ->
                        new ExternalOrigin(rs.getString("origin"), rs.getString("external_id"), rs.getString("url")))
                .list();
    }
}
