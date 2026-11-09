package com.archive.backend.group.infra;

import com.archive.backend.group.read.GroupCatalogEntry;
import com.archive.backend.group.read.GroupDetails;
import com.archive.backend.group.read.GroupLabel;
import com.archive.backend.group.read.GroupLocation;
import com.archive.backend.group.read.GroupMember;
import com.archive.backend.group.read.GroupReadRepository;
import com.archive.backend.group.read.RelatedGroup;
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
 * The group catalog and group pages straight from SQL: the catalog mixes groups and their labels, which no single
 * table holds (docs/domain/authors.md).
 */
@Repository
@RequiredArgsConstructor
public class JdbcGroupReadRepository implements GroupReadRepository {

    /** Groups under their main name, and their other labels; the group's name goes next to each, unless it is the entry. */
    private static final String ENTRIES = "WITH " + MainNameSql.GROUPS + """
            , entries AS (
                SELECT n.group_id, n.main_name AS name, n.group_name
                FROM named_group n
                UNION ALL
                SELECT n.group_id, l.name, n.group_name
                FROM named_group n
                JOIN label l ON l.group_id = n.group_id AND l.id <> n.main_label_id
            )
            """;

    /** The country of a location: given directly, or the one of its city. */
    private static final String LOCATED = """
            EXISTS (
                SELECT 1 FROM entity_location l
                LEFT JOIN city c ON c.id = l.city_id
                WHERE l.entity_type = 'GROUP' AND l.entity_id = e.group_id AND %s
            )""";

    /** The groups related to one, under their main name. */
    private static final String RELATED = "WITH " + MainNameSql.GROUPS + """
            SELECT n.group_id, n.main_name
            FROM group_member_group r
            JOIN named_group n ON n.group_id = r.%s
            WHERE r.%s = :id
            ORDER BY lower(n.main_name), n.group_id
            """;

    private final JdbcClient jdbc;

    @Override
    public Page<GroupCatalogEntry> findCatalogPage(
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
            conditions.add(PlatformFilterSql.condition("e.group_id", "group_id", platform, params));
        }
        String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);

        long total = jdbc.sql(ENTRIES + "SELECT count(*) FROM entries e" + where)
                .params(params)
                .query(Long.class)
                .single();
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());
        List<GroupCatalogEntry> content = jdbc.sql(ENTRIES
                        + "SELECT e.group_id, e.name, CASE WHEN e.name = e.group_name THEN NULL ELSE e.group_name END AS group_name FROM entries e"
                        + where + " ORDER BY lower(e.name), e.name, e.group_id LIMIT :limit OFFSET :offset")
                .params(params)
                .query((rs, row) -> new GroupCatalogEntry(
                        rs.getObject("group_id", UUID.class), rs.getString("name"), rs.getString("group_name")))
                .list();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<GroupDetails> findById(UUID id) {
        return jdbc.sql("WITH " + MainNameSql.GROUPS
                        + "SELECT main_name, group_name, notes FROM named_group WHERE group_id = :id")
                .param("id", id)
                .query((rs, row) -> new GroupDetails(
                        id,
                        rs.getString("main_name"),
                        rs.getString("group_name"),
                        rs.getString("notes"),
                        types(id),
                        labels(id),
                        locations(id),
                        members(id),
                        related(id, "member_group_id", "group_id"),
                        related(id, "group_id", "member_group_id")))
                .optional();
    }

    private List<String> types(UUID groupId) {
        return jdbc.sql("SELECT group_type_code FROM group_group_type WHERE group_id = :id ORDER BY group_type_code")
                .param("id", groupId)
                .query(String.class)
                .list();
    }

    private List<GroupLabel> labels(UUID groupId) {
        return jdbc.sql("SELECT id, name FROM label WHERE group_id = :id ORDER BY lower(name), id")
                .param("id", groupId)
                .query((rs, row) -> new GroupLabel(rs.getObject("id", UUID.class), rs.getString("name")))
                .list();
    }

    /** The primary location first. */
    private List<GroupLocation> locations(UUID groupId) {
        return jdbc.sql("""
                        SELECT co.name AS country, ci.name AS city, COALESCE(l.is_primary, FALSE) AS is_primary
                        FROM entity_location l
                        LEFT JOIN city ci ON ci.id = l.city_id
                        LEFT JOIN country co ON co.id = COALESCE(l.country_id, ci.country_id)
                        WHERE l.entity_type = 'GROUP' AND l.entity_id = :id
                        ORDER BY COALESCE(l.is_primary, FALSE) DESC, co.name, ci.name
                        """)
                .param("id", groupId)
                .query((rs, row) ->
                        new GroupLocation(rs.getString("country"), rs.getString("city"), rs.getBoolean("is_primary")))
                .list();
    }

    /** The members under the labels they were members under, with the person's main name when it is another one. */
    private List<GroupMember> members(UUID groupId) {
        return jdbc.sql("WITH " + MainNameSql.PEOPLE + """
                        SELECT l.name, l.person_id, CASE WHEN p.main_name = l.name THEN NULL ELSE p.main_name END AS person_name
                        FROM group_member gm
                        JOIN label l ON l.id = gm.label_id
                        LEFT JOIN named_person p ON p.person_id = l.person_id
                        WHERE gm.group_id = :id
                        ORDER BY lower(l.name), l.id
                        """)
                .param("id", groupId)
                .query((rs, row) -> new GroupMember(
                        rs.getString("name"), rs.getObject("person_id", UUID.class), rs.getString("person_name")))
                .list();
    }

    /** The groups on the {@code shown} side of the relation, where the group is on the {@code given} side. */
    private List<RelatedGroup> related(UUID groupId, String shown, String given) {
        return jdbc.sql(RELATED.formatted(shown, given))
                .param("id", groupId)
                .query((rs, row) -> new RelatedGroup(rs.getObject("group_id", UUID.class), rs.getString("main_name")))
                .list();
    }
}
