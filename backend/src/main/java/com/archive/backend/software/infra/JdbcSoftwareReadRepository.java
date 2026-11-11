package com.archive.backend.software.infra;

import com.archive.backend.software.read.ReleaseDetails;
import com.archive.backend.software.read.SoftwareCategory;
import com.archive.backend.software.read.SoftwareCategoryStep;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareHardware;
import com.archive.backend.software.read.SoftwarePlatform;
import com.archive.backend.software.read.SoftwareReadRepository;
import com.archive.backend.software.read.SoftwareRelease;
import com.archive.backend.software.read.SoftwareSummary;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
        return page(platform, "created_at DESC, id", Pageable.ofSize(pageable.getPageSize()));
    }

    private Page<SoftwareSummary> page(@Nullable String platform, String order, Pageable pageable) {
        Map<String, Object> params = new HashMap<>();
        String where = "";
        if (platform != null) {
            where = " WHERE platform_code = :platform";
            params.put("platform", platform);
        }
        long total = jdbc.sql("SELECT count(*) FROM software" + where)
                .params(params)
                .query(Long.class)
                .single();
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());
        List<SoftwareSummary> content = jdbc.sql("SELECT id, title, release_date FROM software" + where + " ORDER BY "
                        + order + " LIMIT :limit OFFSET :offset")
                .params(params)
                .query((rs, row) -> new SoftwareSummary(
                        rs.getObject("id", UUID.class), rs.getString("title"), day(rs, "release_date")))
                .list();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<SoftwareDetails> findById(UUID id) {
        return jdbc.sql("""
                        SELECT s.title, s.description, s.release_date, p.code, p.name
                        FROM software s
                        JOIN platform p ON p.code = s.platform_code
                        WHERE s.id = :id
                        """)
                .param("id", id)
                .query((rs, row) -> new SoftwareDetails(
                        id,
                        rs.getString("title"),
                        rs.getString("description"),
                        day(rs, "release_date"),
                        new SoftwarePlatform(rs.getString("code"), rs.getString("name")),
                        hardware("software_hardware", "software_id", id),
                        categories(id)))
                .optional();
    }

    @Override
    public List<SoftwareRelease> findReleases(UUID softwareId) {
        return jdbc.sql("""
                        SELECT r.id, COALESCE(r.title, s.title) AS title, r.version, r.type, r.release_date, r.description
                        FROM software_release r
                        JOIN software s ON s.id = r.software_id
                        WHERE r.software_id = :id
                        ORDER BY r.release_date NULLS LAST, lower(COALESCE(r.title, s.title)), r.id
                        """)
                .param("id", softwareId)
                .query((rs, row) -> {
                    UUID id = rs.getObject("id", UUID.class);
                    return new SoftwareRelease(
                            id,
                            rs.getString("title"),
                            rs.getString("version"),
                            rs.getString("type"),
                            day(rs, "release_date"),
                            rs.getString("description"),
                            hardware("software_release_hardware", "release_id", id));
                })
                .list();
    }

    @Override
    public Optional<ReleaseDetails> findRelease(UUID softwareId, UUID releaseId) {
        return jdbc.sql("""
                        SELECT id, title, version, type, release_date, description
                        FROM software_release
                        WHERE software_id = :softwareId AND id = :releaseId
                        """)
                .param("softwareId", softwareId)
                .param("releaseId", releaseId)
                .query((rs, row) -> new ReleaseDetails(
                        releaseId,
                        rs.getString("title"),
                        rs.getString("version"),
                        rs.getString("type"),
                        day(rs, "release_date"),
                        rs.getString("description"),
                        hardware("software_release_hardware", "release_id", releaseId)))
                .optional();
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

    private static @Nullable LocalDate day(ResultSet rs, String column) throws SQLException {
        Date date = rs.getDate(column);
        return date == null ? null : date.toLocalDate();
    }
}
