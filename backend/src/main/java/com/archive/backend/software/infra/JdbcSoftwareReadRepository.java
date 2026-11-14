package com.archive.backend.software.infra;

import com.archive.backend.label.read.MainNameSql;
import com.archive.backend.software.read.CardMadeBySql;
import com.archive.backend.software.read.CardScreenshotsSql;
import com.archive.backend.software.read.ReleaseArchiveDetails;
import com.archive.backend.software.read.ReleaseDateSql;
import com.archive.backend.software.read.ReleaseDetails;
import com.archive.backend.software.read.SoftwareCategory;
import com.archive.backend.software.read.SoftwareCategoryStep;
import com.archive.backend.software.read.SoftwareCredit;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareHardware;
import com.archive.backend.software.read.SoftwarePlatform;
import com.archive.backend.software.read.SoftwareReadRepository;
import com.archive.backend.software.read.SoftwareReferenceOption;
import com.archive.backend.software.read.SoftwareRelease;
import com.archive.backend.software.read.SoftwareScreenshot;
import com.archive.backend.software.read.SoftwareSource;
import com.archive.backend.software.read.SoftwareSummary;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Software with its platform, hardware and releases straight from SQL: they span several tables.
 */
@Repository
@RequiredArgsConstructor
public class JdbcSoftwareReadRepository implements SoftwareReadRepository {

    /**
     * Hardware in the order of its groups, then by name.
     */
    private static final String HARDWARE_ORDER =
            " ORDER BY array_position(ARRAY['computers', 'storage', 'dos', 'sound', 'controls', 'expansion'],"
                    + " h.hardware_group), lower(h.name), h.code";

    private final JdbcClient jdbc;

    @Override
    public Page<SoftwareSummary> findLatest(@Nullable String platform, Pageable pageable) {
        return page(platform, "s.created_at DESC, s.id", Pageable.ofSize(pageable.getPageSize()));
    }

    private Page<SoftwareSummary> page(@Nullable String platform, String order, Pageable pageable) {
        Map<String, Object> params = new HashMap<>();
        String where = "";
        if (platform != null) {
            where = " WHERE s.platform_code = :platform";
            params.put("platform", platform);
        }
        long total = jdbc.sql("SELECT count(*) FROM software s" + where)
                .params(params)
                .query(Long.class)
                .single();
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());
        List<SoftwareSummary> content = jdbc.sql("SELECT s.id, s.title, " + ReleaseDateSql.of("s")
                        + " AS release_date, " + CardScreenshotsSql.of("s")
                        + " AS screenshots, " + CardMadeBySql.of("s")
                        + " AS made_by FROM software s" + where + " ORDER BY " + order
                        + " LIMIT :limit OFFSET :offset")
                .params(params)
                .query((rs, row) -> new SoftwareSummary(
                        rs.getObject("id", UUID.class),
                        rs.getString("title"),
                        rs.getString("release_date"),
                        screenshots(rs),
                        Arrays.asList((String[]) rs.getArray("made_by").getArray())))
                .list();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<SoftwareDetails> findById(UUID id) {
        return jdbc.sql("""
                        SELECT s.title, s.description, %s AS release_date, p.code, p.name, s.added_by
                        FROM software s
                        JOIN platform p ON p.code = s.platform_code
                        WHERE s.id = :id
                        """.formatted(ReleaseDateSql.of("s")))
                .param("id", id)
                .query((rs, row) -> new SoftwareDetails(
                        id,
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("release_date"),
                        new SoftwarePlatform(rs.getString("code"), rs.getString("name")),
                        hardware("software_hardware", "software_id", id),
                        categories(id),
                        publishers(id),
                        developers(id),
                        credits(id),
                        sources(id),
                        rs.getObject("added_by", UUID.class),
                        false))
                .optional();
    }

    /** The pages of the archives the software was imported from (docs/features/vektor-import.md). */
    private List<SoftwareSource> sources(UUID softwareId) {
        return jdbc.sql("""
                        SELECT origin, url FROM external_origin
                        WHERE kind = 'SOFTWARE' AND entity_id = :id AND url IS NOT NULL
                        ORDER BY origin
                        """)
                .param("id", softwareId)
                .query((rs, row) -> new SoftwareSource(rs.getString("origin"), rs.getString("url")))
                .list();
    }

    private List<SoftwareCredit> credits(UUID softwareId) {
        return jdbc.sql("WITH " + MainNameSql.PEOPLE + """
                        SELECT COALESCE(c.person_id, c.label_id) AS id,
                               CASE WHEN c.person_id IS NULL THEN 'author-label' ELSE 'author' END AS type,
                               CASE WHEN c.person_id IS NOT NULL THEN p.main_name ELSE l.name ||
                                   CASE WHEN p.main_name IS NULL OR p.main_name = l.name THEN '' ELSE ' — ' || p.main_name END END AS title,
                               c.role_codes
                        FROM software_contribution c
                        LEFT JOIN label l ON l.id = c.label_id
                        LEFT JOIN named_person p ON p.person_id = COALESCE(c.person_id, l.person_id)
                        WHERE c.software_id = :id
                        ORDER BY lower(CASE WHEN c.person_id IS NOT NULL THEN p.main_name ELSE l.name END), c.id
                        """)
                .param("id", softwareId)
                .query((rs, row) -> new SoftwareCredit(
                        rs.getObject("id", UUID.class),
                        rs.getString("type"),
                        rs.getString("title"),
                        Arrays.asList((String[]) rs.getArray("role_codes").getArray())))
                .list();
    }

    private List<SoftwareReferenceOption> publishers(UUID softwareId) {
        return jdbc.sql("WITH " + MainNameSql.PEOPLE + ", " + MainNameSql.GROUPS + """
                        , refs AS (
                            SELECT p.person_id AS id, 'author' AS type, p.main_name AS title
                            FROM software_publisher_author r
                            JOIN named_person p ON p.person_id = r.author_id
                            WHERE r.software_id = :id
                            UNION ALL
                            SELECT g.group_id, 'group', g.main_name
                            FROM software_publisher_group r
                            JOIN named_group g ON g.group_id = r.group_id
                            WHERE r.software_id = :id
                            UNION ALL
                            SELECT l.id, 'author-label', l.name || CASE
                                WHEN p.real_name IS NULL OR p.real_name = l.name THEN '' ELSE ' — ' || p.real_name END
                            FROM software_publisher_author_label r
                            JOIN label l ON l.id = r.label_id
                            LEFT JOIN named_person p ON p.person_id = l.person_id
                            WHERE r.software_id = :id
                            UNION ALL
                            SELECT l.id, 'group-label', l.name || CASE
                                WHEN g.group_name IS NULL OR g.group_name = l.name THEN '' ELSE ' — ' || g.group_name END
                            FROM software_publisher_group_label r
                            JOIN label l ON l.id = r.label_id
                            LEFT JOIN named_group g ON g.group_id = l.group_id
                            WHERE r.software_id = :id
                        )
                        SELECT id, type, title FROM refs ORDER BY lower(title), type, id
                        """)
                .param("id", softwareId)
                .query(JdbcSoftwareReadRepository::reference)
                .list();
    }

    private List<SoftwareReferenceOption> developers(UUID softwareId) {
        return jdbc.sql("WITH " + MainNameSql.GROUPS + """
                        , refs AS (
                            SELECT g.group_id AS id, 'group' AS type, g.main_name AS title
                            FROM software_developer_group r
                            JOIN named_group g ON g.group_id = r.group_id
                            WHERE r.software_id = :id
                            UNION ALL
                            SELECT l.id, 'group-label', l.name || CASE
                                WHEN g.group_name IS NULL OR g.group_name = l.name THEN '' ELSE ' — ' || g.group_name END
                            FROM software_developer_label r
                            JOIN label l ON l.id = r.label_id
                            LEFT JOIN named_group g ON g.group_id = l.group_id
                            WHERE r.software_id = :id
                        )
                        SELECT id, type, title FROM refs ORDER BY lower(title), type, id
                        """)
                .param("id", softwareId)
                .query(JdbcSoftwareReadRepository::reference)
                .list();
    }

    private static SoftwareReferenceOption reference(ResultSet rs, int row) throws SQLException {
        return new SoftwareReferenceOption(rs.getObject("id", UUID.class), rs.getString("type"), rs.getString("title"));
    }

    @Override
    public List<SoftwareRelease> findReleases(UUID softwareId) {
        return jdbc.sql("""
                        SELECT r.id, COALESCE(r.title, s.title) AS title, r.version, r.type, %s AS release_date,
                               r.description, r.archive_id, r.archive_file_name, r.archive_size, r.added_by
                        FROM software_release r
                        JOIN software s ON s.id = r.software_id
                        WHERE r.software_id = :id
                        ORDER BY %s, lower(COALESCE(r.title, s.title)), r.id
                        """.formatted(ReleaseDateSql.of("r"), ReleaseDateSql.order("r")))
                .param("id", softwareId)
                .query((rs, row) -> {
                    UUID id = rs.getObject("id", UUID.class);
                    return new SoftwareRelease(
                            id,
                            rs.getString("title"),
                            rs.getString("version"),
                            rs.getString("type"),
                            rs.getString("release_date"),
                            rs.getString("description"),
                            hardware("software_release_hardware", "release_id", id),
                            findReleaseScreenshots(id),
                            archive(rs),
                            rs.getObject("added_by", UUID.class),
                            false);
                })
                .list();
    }

    @Override
    public Optional<ReleaseDetails> findRelease(UUID softwareId, UUID releaseId) {
        return jdbc.sql("""
                        SELECT r.id, r.title, r.version, r.type, %s AS release_date, r.description,
                               r.archive_id, r.archive_file_name, r.archive_size
                        FROM software_release r
                        WHERE r.software_id = :softwareId AND r.id = :releaseId
                        """.formatted(ReleaseDateSql.of("r")))
                .param("softwareId", softwareId)
                .param("releaseId", releaseId)
                .query((rs, row) -> new ReleaseDetails(
                        releaseId,
                        rs.getString("title"),
                        rs.getString("version"),
                        rs.getString("type"),
                        rs.getString("release_date"),
                        rs.getString("description"),
                        hardware("software_release_hardware", "release_id", releaseId),
                        archive(rs)))
                .optional();
    }

    private static @Nullable ReleaseArchiveDetails archive(ResultSet rs) throws SQLException {
        UUID id = rs.getObject("archive_id", UUID.class);
        return id == null
                ? null
                : new ReleaseArchiveDetails(id, rs.getString("archive_file_name"), rs.getLong("archive_size"));
    }

    /**
     * The categories of software in the order of the tree, each by the positions of its line from the top, with its
     * branch from the top down to itself.
     */
    private List<SoftwareCategory> categories(UUID softwareId) {
        return jdbc.sql("""
                        WITH RECURSIVE tree (id, title, path, ids, titles) AS (
                            SELECT id, title, ARRAY[position], ARRAY[id], ARRAY[title]
                            FROM category
                            WHERE parent_id IS NULL
                            UNION ALL
                            SELECT c.id, c.title, t.path || c.position, t.ids || c.id, t.titles || c.title
                            FROM category c
                            JOIN tree t ON c.parent_id = t.id
                        )
                        SELECT t.id, t.title, t.ids, t.titles
                        FROM tree t
                        JOIN software_category sc ON sc.category_id = t.id
                        WHERE sc.software_id = :id
                        ORDER BY t.path, t.title, t.id
                        """)
                .param("id", softwareId)
                .query((rs, row) ->
                        new SoftwareCategory(rs.getObject("id", UUID.class), rs.getString("title"), branch(rs)))
                .list();
    }

    private static List<SoftwareCategoryStep> branch(ResultSet rs) throws SQLException {
        UUID[] ids = (UUID[]) rs.getArray("ids").getArray();
        String[] titles = (String[]) rs.getArray("titles").getArray();
        List<SoftwareCategoryStep> branch = new ArrayList<>(ids.length);
        for (int i = 0; i < ids.length; i++) {
            branch.add(new SoftwareCategoryStep(ids[i], titles[i]));
        }
        return branch;
    }

    /**
     * The hardware attached to one row of the owner table.
     */
    private List<SoftwareHardware> hardware(String table, String ownerColumn, UUID ownerId) {
        return jdbc.sql("SELECT h.code, h.name, h.hardware_group FROM " + table
                        + " x JOIN hardware h ON h.code = x.hardware_code WHERE x." + ownerColumn + " = :id"
                        + HARDWARE_ORDER)
                .param("id", ownerId)
                .query((rs, row) -> new SoftwareHardware(
                        rs.getString("code"), rs.getString("name"), rs.getString("hardware_group")))
                .list();
    }

    @Override
    public List<SoftwareScreenshot> findScreenshots(UUID softwareId) {
        return screenshots("software_screenshot", "software_id", softwareId);
    }

    @Override
    public List<SoftwareScreenshot> findReleaseScreenshots(UUID releaseId) {
        return screenshots("software_release_screenshot", "release_id", releaseId);
    }

    private List<SoftwareScreenshot> screenshots(String table, String ownerColumn, UUID ownerId) {
        return jdbc.sql("SELECT id FROM " + table + " WHERE " + ownerColumn + " = :id ORDER BY position")
                .param("id", ownerId)
                .query((rs, row) -> new SoftwareScreenshot(rs.getObject("id", UUID.class)))
                .list();
    }

    private static List<SoftwareScreenshot> screenshots(ResultSet rs) throws SQLException {
        return Arrays.stream((UUID[]) rs.getArray("screenshots").getArray())
                .map(SoftwareScreenshot::new)
                .toList();
    }
}
