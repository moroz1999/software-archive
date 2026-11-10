package com.archive.backend.author.infra;

import com.archive.backend.author.read.AuthorCatalogEntry;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.author.read.AuthorGroup;
import com.archive.backend.author.read.AuthorLabel;
import com.archive.backend.author.read.AuthorLocation;
import com.archive.backend.author.read.AuthorReadRepository;
import com.archive.backend.label.read.LetterFilterSql;
import com.archive.backend.label.read.MainNameSql;
import com.archive.backend.label.read.PlatformFilterSql;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * The author catalog and author pages straight from SQL: the catalog mixes people and their labels, which no
 * single table holds (docs/domain/authors.md).
 */
@Repository
@RequiredArgsConstructor
public class JdbcAuthorReadRepository implements AuthorReadRepository {

    /**
     * People under their main name, and their other labels with the main name; the real name goes next to each,
     * except next to a person shown under it.
     */
    private static final String ENTRIES = "WITH " + MainNameSql.PEOPLE + """
            , entries AS (
                SELECT n.person_id, n.main_name AS name,
                       CASE WHEN n.main_label_id IS NULL THEN NULL ELSE n.real_name END AS real_name,
                       NULL AS label_of
                FROM named_person n
                WHERE n.main_name IS NOT NULL
                UNION ALL
                SELECT n.person_id, l.name, n.real_name, n.main_name
                FROM named_person n
                JOIN label l ON l.person_id = n.person_id AND l.id <> n.main_label_id
            )
            """;

    /**
     * The country of a location: given directly, or the one of its city.
     */
    private static final String LOCATED = """
            EXISTS (
                SELECT 1 FROM entity_location l
                LEFT JOIN city c ON c.id = l.city_id
                WHERE l.entity_type = 'PERSON' AND l.entity_id = e.person_id AND %s
            )""";

    private final JdbcClient jdbc;

    @Override
    public Page<AuthorCatalogEntry> findCatalogPage(
            @Nullable String letter,
            @Nullable UUID countryId,
            @Nullable UUID cityId,
            @Nullable String platform,
            Pageable pageable) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        if (letter != null) {
            conditions.add(LetterFilterSql.condition("e.name", letter, params));
        }
        if (countryId != null) {
            conditions.add(LOCATED.formatted("COALESCE(l.country_id, c.country_id) = :country"));
            params.put("country", countryId);
        }
        if (cityId != null) {
            conditions.add(LOCATED.formatted("l.city_id = :city"));
            params.put("city", cityId);
        }
        if (platform != null) {
            conditions.add(PlatformFilterSql.condition("e.person_id", "person_id", platform, params));
        }
        String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);

        long total = jdbc.sql(ENTRIES + "SELECT count(*) FROM entries e" + where)
                .params(params)
                .query(Long.class)
                .single();
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());
        List<AuthorCatalogEntry> content = jdbc.sql(
                        ENTRIES + "SELECT e.person_id, e.name, e.real_name, e.label_of FROM entries e" + where
                                + " ORDER BY lower(e.name), e.name, e.person_id LIMIT :limit OFFSET :offset")
                .params(params)
                .query((rs, row) -> new AuthorCatalogEntry(
                        rs.getObject("person_id", UUID.class),
                        rs.getString("name"),
                        rs.getString("real_name"),
                        rs.getString("label_of")))
                .list();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<AuthorDetails> findById(UUID id) {
        return jdbc.sql(
                        "WITH " + MainNameSql.PEOPLE
                                + "SELECT main_name, real_name, notes FROM named_person WHERE person_id = :id AND main_name IS NOT NULL")
                .param("id", id)
                .query((rs, row) -> new AuthorDetails(
                        id,
                        rs.getString("main_name"),
                        rs.getString("real_name"),
                        rs.getString("notes"),
                        labels(id),
                        locations(id),
                        groups(id)))
                .optional();
    }

    private List<AuthorLabel> labels(UUID personId) {
        return jdbc.sql("SELECT id, name FROM label WHERE person_id = :id ORDER BY lower(name), id")
                .param("id", personId)
                .query((rs, row) -> new AuthorLabel(rs.getObject("id", UUID.class), rs.getString("name")))
                .list();
    }

    /**
     * The primary location first.
     */
    private List<AuthorLocation> locations(UUID personId) {
        return jdbc.sql("""
                        SELECT co.name AS country, ci.name AS city, COALESCE(l.is_primary, FALSE) AS is_primary
                        FROM entity_location l
                        LEFT JOIN city ci ON ci.id = l.city_id
                        LEFT JOIN country co ON co.id = COALESCE(l.country_id, ci.country_id)
                        WHERE l.entity_type = 'PERSON' AND l.entity_id = :id
                        ORDER BY COALESCE(l.is_primary, FALSE) DESC, co.name, ci.name
                        """)
                .param("id", personId)
                .query((rs, row) ->
                        new AuthorLocation(rs.getString("country"), rs.getString("city"), rs.getBoolean("is_primary")))
                .list();
    }

    /**
     * Every membership of the person, the group under its main name, with the label they were a member under unless
     * it is their main name.
     */
    private List<AuthorGroup> groups(UUID personId) {
        return jdbc.sql("WITH " + MainNameSql.PEOPLE + ", " + MainNameSql.GROUPS + """
                        SELECT g.group_id, g.main_name, CASE WHEN l.name = p.main_name THEN NULL ELSE l.name END AS member_as
                        FROM group_member gm
                        JOIN label l ON l.id = gm.label_id
                        JOIN named_person p ON p.person_id = l.person_id
                        JOIN named_group g ON g.group_id = gm.group_id
                        WHERE l.person_id = :id
                        ORDER BY lower(g.main_name), g.group_id, lower(l.name), l.id
                        """)
                .param("id", personId)
                .query((rs, row) -> new AuthorGroup(
                        rs.getObject("group_id", UUID.class), rs.getString("main_name"), rs.getString("member_as")))
                .list();
    }
}
