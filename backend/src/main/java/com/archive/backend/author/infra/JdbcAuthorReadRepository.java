package com.archive.backend.author.infra;

import com.archive.backend.author.read.AuthorCatalogEntry;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.author.read.AuthorGroup;
import com.archive.backend.author.read.AuthorLabel;
import com.archive.backend.author.read.AuthorLocation;
import com.archive.backend.author.read.AuthorReadRepository;
import com.archive.backend.geo.read.LocationsSql;
import com.archive.backend.label.read.LetterFilterSql;
import com.archive.backend.label.read.MainNameSql;
import com.archive.backend.label.read.PlatformFilterSql;
import com.archive.backend.user.read.AdditionSql;
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
     * People with a real name under it, their main label next to it; every label of a person, the real name next to
     * it, else the main name unless it is that name; and the labels of nobody. {@code owner_id} is the person of an
     * entry, for the filters.
     */
    private static final String ENTRIES = "WITH " + MainNameSql.PEOPLE + """
            , entries AS (
                SELECT n.person_id AS owner_id, n.person_id, NULL::uuid AS label_id, n.real_name AS name,
                       CASE WHEN n.main_label_id IS NULL THEN NULL ELSE n.main_name END AS note
                FROM named_person n
                WHERE n.real_name IS NOT NULL
                UNION ALL
                SELECT n.person_id, NULL::uuid, l.id, l.name,
                       COALESCE(n.real_name, CASE WHEN l.id = n.main_label_id THEN NULL ELSE n.main_name END)
                FROM named_person n
                JOIN label l ON l.person_id = n.person_id
                UNION ALL
                SELECT NULL::uuid, NULL::uuid, l.id, l.name, NULL
                FROM label l
                WHERE l.person_id IS NULL AND l.group_id IS NULL
            )
            """;

    /** A label of nobody is kept for a platform it is credited on itself. */
    private static final String NOBODYS_LABEL_ON_PLATFORM = """
            EXISTS (
                SELECT 1 FROM software_contribution sc
                JOIN software s ON s.id = sc.software_id
                WHERE sc.label_id = e.label_id AND s.platform_code = :platform
            )""";

    /**
     * The country of a location: given directly, or the one of its city.
     */
    private static final String LOCATED = """
            EXISTS (
                SELECT 1 FROM entity_location l
                LEFT JOIN city c ON c.id = l.city_id
                WHERE l.entity_type = 'PERSON' AND l.entity_id = e.owner_id AND %s
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
            conditions.add("(" + PlatformFilterSql.condition("e.owner_id", "person_id", platform, params) + " OR "
                    + NOBODYS_LABEL_ON_PLATFORM + ")");
        }
        String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);

        long total = jdbc.sql(ENTRIES + "SELECT count(*) FROM entries e" + where)
                .params(params)
                .query(Long.class)
                .single();
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());
        List<AuthorCatalogEntry> content = jdbc.sql(
                        ENTRIES
                                + "SELECT e.person_id, e.label_id, e.name, e.note FROM entries e" + where
                                + " ORDER BY lower(e.name), e.name, e.owner_id, e.label_id NULLS FIRST LIMIT :limit OFFSET :offset")
                .params(params)
                .query((rs, row) -> new AuthorCatalogEntry(
                        rs.getObject("person_id", UUID.class),
                        rs.getObject("label_id", UUID.class),
                        rs.getString("name"),
                        rs.getString("note")))
                .list();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<AuthorDetails> findById(UUID id, String language) {
        return jdbc.sql("WITH " + MainNameSql.PEOPLE
                        + "SELECT n.main_name, n.real_name, n.notes, p.added_by, " + AdditionSql.columns("p")
                        + " FROM named_person n JOIN person p ON p.id = n.person_id"
                        + " WHERE n.person_id = :id AND n.main_name IS NOT NULL")
                .param("id", id)
                .query((rs, row) -> new AuthorDetails(
                        id,
                        rs.getString("main_name"),
                        rs.getString("real_name"),
                        rs.getString("notes"),
                        labels(id),
                        locations(id, language),
                        groups(id),
                        AdditionSql.of(rs),
                        rs.getObject("added_by", UUID.class),
                        false))
                .optional();
    }

    /**
     * The label shown as the main name is the display name, also of a record from before the rule with none marked.
     */
    private List<AuthorLabel> labels(UUID personId) {
        return jdbc.sql("WITH " + MainNameSql.PEOPLE + """
                        SELECT l.id, l.name, l.id = n.main_label_id AS display
                        FROM label l
                        JOIN named_person n ON n.person_id = l.person_id
                        WHERE l.person_id = :id
                        ORDER BY lower(l.name), l.id
                        """)
                .param("id", personId)
                .query((rs, row) ->
                        new AuthorLabel(rs.getObject("id", UUID.class), rs.getString("name"), rs.getBoolean("display")))
                .list();
    }

    private List<AuthorLocation> locations(UUID personId, String language) {
        return jdbc.sql(LocationsSql.of("PERSON"))
                .param("id", personId)
                .param("language", language)
                .query((rs, row) -> new AuthorLocation(
                        rs.getObject("country_id", UUID.class),
                        rs.getString("country"),
                        rs.getObject("city_id", UUID.class),
                        rs.getString("city"),
                        rs.getBoolean("is_primary")))
                .list();
    }

    /**
     * Every membership of the person, the group under its main name, with the label they were a member under unless
     * it is their main name.
     */
    private List<AuthorGroup> groups(UUID personId) {
        return jdbc.sql("WITH " + MainNameSql.PEOPLE + ", " + MainNameSql.GROUPS + """
                        SELECT g.group_id, g.main_name,
                               CASE WHEN gm.person_id IS NOT NULL OR l.name = p.main_name THEN NULL ELSE l.name END AS member_as
                        FROM group_member gm
                        LEFT JOIN label l ON l.id = gm.label_id
                        JOIN named_person p ON p.person_id = COALESCE(gm.person_id, l.person_id)
                        JOIN named_group g ON g.group_id = gm.group_id
                        WHERE COALESCE(gm.person_id, l.person_id) = :id
                        ORDER BY lower(g.main_name), g.group_id, lower(l.name) NULLS FIRST, l.id
                        """)
                .param("id", personId)
                .query((rs, row) -> new AuthorGroup(
                        rs.getObject("group_id", UUID.class), rs.getString("main_name"), rs.getString("member_as")))
                .list();
    }
}
