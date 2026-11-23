package com.archive.backend.software.infra;

import com.archive.backend.category.read.CategoryTitleSql;
import com.archive.backend.hardware.read.HardwareNameSql;
import com.archive.backend.label.read.MainNameSql;
import com.archive.backend.software.read.CardMadeBySql;
import com.archive.backend.software.read.CardScreenshotsSql;
import com.archive.backend.software.read.CreditedRelease;
import com.archive.backend.software.read.CreditedSoftware;
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
import com.archive.backend.software.read.SoftwareRelease;
import com.archive.backend.software.read.SoftwareScreenshot;
import com.archive.backend.software.read.SoftwareSource;
import com.archive.backend.software.read.SoftwareSummary;
import com.archive.backend.user.read.AdditionSql;
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
    private static final String HARDWARE_ORDER = " ORDER BY h.position, lower(h.name), h.code";

    /** The credits of software or of releases: their table and the column of what they credit. */
    private record Credited(String table, String of) {}

    private static final Credited SOFTWARE_CREDITS = new Credited("software_contribution", "software_id");
    private static final Credited RELEASE_CREDITS = new Credited("software_release_contribution", "release_id");

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
    public boolean exists(UUID id) {
        return Boolean.TRUE.equals(jdbc.sql("SELECT EXISTS (SELECT 1 FROM software WHERE id = :id)")
                .param("id", id)
                .query(Boolean.class)
                .single());
    }

    @Override
    public boolean releaseExists(UUID softwareId, UUID releaseId) {
        return Boolean.TRUE.equals(jdbc.sql(
                        "SELECT EXISTS (SELECT 1 FROM software_release WHERE software_id = :softwareId AND id = :releaseId)")
                .param("softwareId", softwareId)
                .param("releaseId", releaseId)
                .query(Boolean.class)
                .single());
    }

    @Override
    public Optional<SoftwareDetails> findById(UUID id, String language) {
        return jdbc.sql("""
                        SELECT s.title, s.description, s.preservation, %s AS release_date, p.code, %s AS name, s.added_by, %s
                        FROM software s
                        JOIN platform p ON p.code = s.platform_code
                        WHERE s.id = :id
                        """.formatted(
                                ReleaseDateSql.of("s"), HardwareNameSql.platform("p.code"), AdditionSql.columns("s")))
                .param("id", id)
                .param("language", language)
                .query((rs, row) -> new SoftwareDetails(
                        id,
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("release_date"),
                        rs.getString("preservation"),
                        new SoftwarePlatform(rs.getString("code"), rs.getString("name")),
                        hardware("software_hardware", "software_id", id, language),
                        categories(id, language),
                        credits(SOFTWARE_CREDITS, id),
                        sources(id),
                        AdditionSql.of(rs),
                        rs.getObject("added_by", UUID.class),
                        false))
                .optional();
    }

    @Override
    public List<CreditedSoftware> findCreditedOnPerson(UUID personId) {
        return cards(creditedOn(SOFTWARE_CREDITS, "person_id", MainNameSql.PEOPLE, "named_person", personId));
    }

    @Override
    public List<CreditedSoftware> findCreditedOnGroup(UUID groupId) {
        return cards(creditedOn(SOFTWARE_CREDITS, "group_id", MainNameSql.GROUPS, "named_group", groupId));
    }

    @Override
    public List<CreditedSoftware> findCreditedOnLabel(UUID labelId) {
        return cards(creditedOnLabel(SOFTWARE_CREDITS, labelId));
    }

    @Override
    public List<CreditedRelease> findReleasesCreditedOnPerson(UUID personId) {
        return releaseCards(creditedOn(RELEASE_CREDITS, "person_id", MainNameSql.PEOPLE, "named_person", personId));
    }

    @Override
    public List<CreditedRelease> findReleasesCreditedOnGroup(UUID groupId) {
        return releaseCards(creditedOn(RELEASE_CREDITS, "group_id", MainNameSql.GROUPS, "named_group", groupId));
    }

    @Override
    public List<CreditedRelease> findReleasesCreditedOnLabel(UUID labelId) {
        return releaseCards(creditedOnLabel(RELEASE_CREDITS, labelId));
    }

    /** The credits of a label by itself, by the software or the release they are on. */
    private Map<UUID, List<CreditedSoftware.OwnCredit>> creditedOnLabel(Credited credited, UUID labelId) {
        return ownCredits("""
                        SELECT %s AS work_id, NULL AS as_label, role_codes
                        FROM %s
                        WHERE label_id = :owner
                        """.formatted(credited.of(), credited.table()), labelId);
    }

    /**
     * The credits of an owner, by the owner column of the credits and of their labels, by the software or the release
     * they are on: by themselves first, then by label in name order; a label is named unless it is the main name.
     */
    private Map<UUID, List<CreditedSoftware.OwnCredit>> creditedOn(
            Credited credited, String ownerColumn, String mainNames, String named, UUID ownerId) {
        return ownCredits(
                "WITH " + mainNames + """
                        SELECT sc.%3$s AS work_id,
                               CASE WHEN sc.label_id IS NULL OR sc.label_id = n.main_label_id THEN NULL ELSE l.name END
                                   AS as_label,
                               sc.role_codes
                        FROM %4$s sc
                        LEFT JOIN label l ON l.id = sc.label_id
                        JOIN %2$s n ON n.%1$s = COALESCE(sc.%1$s, l.%1$s)
                        WHERE n.%1$s = :owner
                        ORDER BY sc.label_id IS NOT NULL, lower(l.name), sc.id
                        """.formatted(ownerColumn, named, credited.of(), credited.table()), ownerId);
    }

    /** The credits the query of {@code :owner} finds, by the software or the release they are on, in its order. */
    private Map<UUID, List<CreditedSoftware.OwnCredit>> ownCredits(String sql, UUID ownerId) {
        Map<UUID, List<CreditedSoftware.OwnCredit>> credits = new HashMap<>();
        jdbc.sql(sql)
                .param("owner", ownerId)
                .query((rs, row) -> {
                    credits.computeIfAbsent(rs.getObject("work_id", UUID.class), id -> new ArrayList<>())
                            .add(new CreditedSoftware.OwnCredit(rs.getString("as_label"), Arrays.asList((String[])
                                    rs.getArray("role_codes").getArray())));
                    return row;
                })
                .list();
        return credits;
    }

    /** The credited releases, by release date, then title, with the credits on each. */
    private List<CreditedRelease> releaseCards(Map<UUID, List<CreditedSoftware.OwnCredit>> credits) {
        if (credits.isEmpty()) {
            return List.of();
        }
        return jdbc.sql("""
                        SELECT r.id, r.software_id, s.title AS software_title, COALESCE(r.title, s.title) AS title,
                               r.version, r.type, %s AS release_date
                        FROM software_release r
                        JOIN software s ON s.id = r.software_id
                        WHERE r.id IN (:ids)
                        ORDER BY %s, lower(COALESCE(r.title, s.title)), r.id
                        """.formatted(ReleaseDateSql.of("r"), ReleaseDateSql.order("r")))
                .param("ids", credits.keySet())
                .query((rs, row) -> {
                    UUID id = rs.getObject("id", UUID.class);
                    return new CreditedRelease(
                            id,
                            rs.getObject("software_id", UUID.class),
                            rs.getString("software_title"),
                            rs.getString("title"),
                            rs.getString("version"),
                            rs.getString("type"),
                            rs.getString("release_date"),
                            findReleaseScreenshots(id),
                            List.copyOf(credits.getOrDefault(id, List.of())));
                })
                .list();
    }

    /** The cards of the credited software, by release date, then title, with the credits on each. */
    private List<CreditedSoftware> cards(Map<UUID, List<CreditedSoftware.OwnCredit>> credits) {
        if (credits.isEmpty()) {
            return List.of();
        }
        return jdbc.sql("SELECT s.id, s.title, " + ReleaseDateSql.of("s") + " AS release_date, "
                        + CardScreenshotsSql.of("s") + " AS screenshots, " + CardMadeBySql.of("s")
                        + " AS made_by FROM software s WHERE s.id IN (:ids) ORDER BY " + ReleaseDateSql.order("s")
                        + ", lower(s.title), s.id")
                .param("ids", credits.keySet())
                .query((rs, row) -> {
                    UUID id = rs.getObject("id", UUID.class);
                    return new CreditedSoftware(
                            id,
                            rs.getString("title"),
                            rs.getString("release_date"),
                            screenshots(rs),
                            Arrays.asList((String[]) rs.getArray("made_by").getArray()),
                            List.copyOf(credits.getOrDefault(id, List.of())));
                })
                .list();
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

    /**
     * Everyone credited: a person or a group by the main name, a label by itself with the name of its owner beside it,
     * in the order of those names.
     */
    private List<SoftwareCredit> credits(Credited credited, UUID id) {
        return jdbc.sql("WITH " + MainNameSql.PEOPLE + ", " + MainNameSql.GROUPS
                        + """
                        SELECT COALESCE(c.person_id, c.group_id, c.label_id) AS id,
                               COALESCE(c.person_id, c.group_id, l.person_id, l.group_id) AS owner_id,
                               CASE WHEN c.person_id IS NOT NULL THEN 'author'
                                    WHEN c.group_id IS NOT NULL THEN 'group'
                                    WHEN l.group_id IS NOT NULL THEN 'group-label'
                                    ELSE 'author-label' END AS type,
                               CASE WHEN c.person_id IS NOT NULL THEN p.main_name
                                    WHEN c.group_id IS NOT NULL THEN g.main_name
                                    WHEN l.group_id IS NOT NULL THEN l.name ||
                                        CASE WHEN g.group_name = l.name THEN '' ELSE ' — ' || g.group_name END
                                    ELSE l.name ||
                                        CASE WHEN p.main_name IS NULL OR p.main_name = l.name THEN '' ELSE ' — ' || p.main_name END
                               END AS title,
                               c.role_codes
                        FROM %s c
                        LEFT JOIN label l ON l.id = c.label_id
                        LEFT JOIN named_person p ON p.person_id = COALESCE(c.person_id, l.person_id)
                        LEFT JOIN named_group g ON g.group_id = COALESCE(c.group_id, l.group_id)
                        WHERE c.%s = :id
                        ORDER BY lower(COALESCE(l.name, p.main_name, g.main_name)), c.id
                        """.formatted(credited.table(), credited.of()))
                .param("id", id)
                .query((rs, row) -> new SoftwareCredit(
                        rs.getObject("id", UUID.class),
                        rs.getObject("owner_id", UUID.class),
                        rs.getString("type"),
                        rs.getString("title"),
                        Arrays.asList((String[]) rs.getArray("role_codes").getArray())))
                .list();
    }

    @Override
    public List<SoftwareRelease> findReleases(UUID softwareId, String language) {
        return jdbc.sql("""
                        SELECT r.id, COALESCE(r.title, s.title) AS title, r.version, r.type, %s AS release_date,
                               r.description, r.archive_id, r.archive_file_name, r.archive_size, r.added_by,
                               %s
                        FROM software_release r
                        JOIN software s ON s.id = r.software_id
                        WHERE r.software_id = :id
                        ORDER BY %s, lower(COALESCE(r.title, s.title)), r.id
                        """.formatted(ReleaseDateSql.of("r"), AdditionSql.columns("r"), ReleaseDateSql.order("r")))
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
                            hardware("software_release_hardware", "release_id", id, language),
                            findReleaseScreenshots(id),
                            archive(rs),
                            AdditionSql.of(rs),
                            rs.getObject("added_by", UUID.class),
                            false,
                            credits(RELEASE_CREDITS, id));
                })
                .list();
    }

    @Override
    public Optional<ReleaseDetails> findRelease(UUID softwareId, UUID releaseId, String language) {
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
                        hardware("software_release_hardware", "release_id", releaseId, language),
                        archive(rs),
                        credits(RELEASE_CREDITS, releaseId)))
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
    private List<SoftwareCategory> categories(UUID softwareId, String language) {
        return jdbc.sql("""
                        WITH RECURSIVE titled (id, parent_id, position, title) AS (
                            SELECT c.id, c.parent_id, c.position, %s FROM category c
                        ),
                        tree (id, title, path, ids, titles) AS (
                            SELECT id, title, ARRAY[position], ARRAY[id], ARRAY[title]
                            FROM titled
                            WHERE parent_id IS NULL
                            UNION ALL
                            SELECT c.id, c.title, t.path || c.position, t.ids || c.id, t.titles || c.title
                            FROM titled c
                            JOIN tree t ON c.parent_id = t.id
                        )
                        SELECT t.id, t.title, t.ids, t.titles
                        FROM tree t
                        JOIN software_category sc ON sc.category_id = t.id
                        WHERE sc.software_id = :id
                        ORDER BY t.path, t.title, t.id
                        """.formatted(CategoryTitleSql.of("c.id")))
                .param("id", softwareId)
                .param("language", language)
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
    private List<SoftwareHardware> hardware(String table, String ownerColumn, UUID ownerId, String language) {
        return jdbc.sql("SELECT h.code, h.name, h.hardware_group, h.group_name, h.position FROM (SELECT hw.code, "
                        + HardwareNameSql.hardware("hw.code") + " AS name, hw.hardware_group, "
                        + HardwareNameSql.group("g.code") + " AS group_name, g.position FROM " + table
                        + " x JOIN hardware hw ON hw.code = x.hardware_code"
                        + " JOIN hardware_group g ON g.code = hw.hardware_group WHERE x." + ownerColumn + " = :id) h"
                        + HARDWARE_ORDER)
                .param("id", ownerId)
                .param("language", language)
                .query((rs, row) -> new SoftwareHardware(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("hardware_group"),
                        rs.getString("group_name"),
                        rs.getInt("position")))
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
