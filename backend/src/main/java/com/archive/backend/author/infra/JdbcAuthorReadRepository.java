package com.archive.backend.author.infra;

import com.archive.backend.author.read.AuthorAlias;
import com.archive.backend.author.read.AuthorCatalogEntry;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.author.read.AuthorGroup;
import com.archive.backend.author.read.AuthorLocation;
import com.archive.backend.author.read.AuthorReadRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The author catalog and author pages straight from SQL: the catalog mixes people and their aliases, which no
 * single table holds (docs/domain/authors.md).
 */
@Repository
@RequiredArgsConstructor
public class JdbcAuthorReadRepository implements AuthorReadRepository {

    /**
     * Every person with their main name and the alias it comes from: the display name alias, else the first alias
     * by name, else the real name (then no alias).
     */
    private static final String NAMED_PEOPLE = """
            WITH main_alias AS (
                SELECT DISTINCT ON (a.person_id) a.person_id, a.id, a.name
                FROM alias a
                WHERE a.person_id IS NOT NULL
                ORDER BY a.person_id, a.is_display_name DESC NULLS LAST, lower(a.name), a.id
            ), named AS (
                SELECT p.id AS person_id, COALESCE(m.name, p.real_name) AS main_name, m.id AS main_alias_id,
                       p.real_name, p.notes
                FROM person p
                LEFT JOIN main_alias m ON m.person_id = p.id
            )
            """;

    /** People under their main name, and their other aliases with the main name. */
    private static final String ENTRIES = NAMED_PEOPLE + """
            , entries AS (
                SELECT n.person_id, n.main_name AS name, NULL AS alias_of
                FROM named n
                WHERE n.main_name IS NOT NULL
                UNION ALL
                SELECT n.person_id, a.name, n.main_name
                FROM named n
                JOIN alias a ON a.person_id = n.person_id AND a.id <> n.main_alias_id
            )
            """;

    /** The country of a location: given directly, or the one of its city. */
    private static final String LOCATED = """
            EXISTS (
                SELECT 1 FROM entity_location l
                LEFT JOIN city c ON c.id = l.city_id
                WHERE l.entity_type = 'PERSON' AND l.entity_id = e.person_id AND %s
            )""";

    private static final String REST_LETTER = "#";
    /** First characters that are catalog letters; anything else is under the rest sign. */
    private static final String LETTERS_PATTERN = "^[A-Za-zА-Яа-яЁё]";

    private final JdbcClient jdbc;

    @Override
    public Page<AuthorCatalogEntry> findCatalogPage(@Nullable String letter, @Nullable UUID countryId, @Nullable UUID cityId, Pageable pageable) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        if (letter != null) {
            if (REST_LETTER.equals(letter)) {
                conditions.add("e.name !~ :lettersPattern");
                params.put("lettersPattern", LETTERS_PATTERN);
            } else {
                conditions.add("left(e.name, 1) IN (:firstChars)");
                params.put("firstChars", firstChars(letter));
            }
        }
        if (countryId != null) {
            conditions.add(LOCATED.formatted("COALESCE(l.country_id, c.country_id) = :country"));
            params.put("country", countryId);
        }
        if (cityId != null) {
            conditions.add(LOCATED.formatted("l.city_id = :city"));
            params.put("city", cityId);
        }
        String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);

        long total = jdbc.sql(ENTRIES + "SELECT count(*) FROM entries e" + where)
                .params(params)
                .query(Long.class)
                .single();
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());
        List<AuthorCatalogEntry> content = jdbc.sql(ENTRIES + "SELECT e.person_id, e.name, e.alias_of FROM entries e" + where
                        + " ORDER BY lower(e.name), e.name, e.person_id LIMIT :limit OFFSET :offset")
                .params(params)
                .query((rs, row) -> new AuthorCatalogEntry(rs.getObject("person_id", UUID.class), rs.getString("name"), rs.getString("alias_of")))
                .list();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<AuthorDetails> findById(UUID id) {
        return jdbc.sql(NAMED_PEOPLE + "SELECT main_name, real_name, notes FROM named WHERE person_id = :id AND main_name IS NOT NULL")
                .param("id", id)
                .query((rs, row) -> new AuthorDetails(
                        id, rs.getString("main_name"), rs.getString("real_name"), rs.getString("notes"),
                        aliases(id), locations(id), groups(id)))
                .optional();
    }

    /** The letter in both cases; Е also stands for Ё. */
    private static List<String> firstChars(String letter) {
        List<String> chars = new ArrayList<>(List.of(letter, letter.toLowerCase(Locale.ROOT)));
        if ("Е".equals(letter)) {
            chars.addAll(List.of("Ё", "ё"));
        }
        return chars;
    }

    private List<AuthorAlias> aliases(UUID personId) {
        return jdbc.sql("SELECT id, name FROM alias WHERE person_id = :id ORDER BY lower(name), id")
                .param("id", personId)
                .query((rs, row) -> new AuthorAlias(rs.getObject("id", UUID.class), rs.getString("name")))
                .list();
    }

    /** The primary location first. */
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
                .query((rs, row) -> new AuthorLocation(rs.getString("country"), rs.getString("city"), rs.getBoolean("is_primary")))
                .list();
    }

    /** The groups any alias of the person is a member of. */
    private List<AuthorGroup> groups(UUID personId) {
        return jdbc.sql("""
                        SELECT DISTINCT g.id, g.name
                        FROM group_member gm
                        JOIN alias a ON a.id = gm.alias_id
                        JOIN "group" g ON g.id = gm.group_id
                        WHERE a.person_id = :id
                        ORDER BY g.name, g.id
                        """)
                .param("id", personId)
                .query((rs, row) -> new AuthorGroup(rs.getObject("id", UUID.class), rs.getString("name")))
                .list();
    }
}
