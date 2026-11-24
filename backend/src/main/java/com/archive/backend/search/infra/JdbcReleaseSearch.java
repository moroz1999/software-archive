package com.archive.backend.search.infra;

import com.archive.backend.search.read.FoundRelease;
import com.archive.backend.search.read.ReleaseSearch;
import com.archive.backend.search.read.ReleaseSearchFilter;
import com.archive.backend.software.read.ReleaseDateSql;
import com.archive.backend.software.read.SoftwareReadRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * The release search straight from SQL. The hardware of a release is its own and its software's in the groups it does
 * not state, as everywhere in the archive (docs/domain/hardware.md).
 */
@Repository
@RequiredArgsConstructor
public class JdbcReleaseSearch implements ReleaseSearch {

    /** Every hardware code of each release: its own, and its software's of the groups it does not state. */
    private static final String HARDWARE = """
            release_hardware AS (
                SELECT rh.release_id, rh.hardware_code AS code FROM software_release_hardware rh
                UNION
                SELECT r.id, sh.hardware_code
                FROM software_release r
                JOIN software_hardware sh ON sh.software_id = r.software_id
                JOIN hardware h ON h.code = sh.hardware_code
                WHERE NOT EXISTS (
                    SELECT 1 FROM software_release_hardware own
                    JOIN hardware oh ON oh.code = own.hardware_code
                    WHERE own.release_id = r.id AND oh.hardware_group = h.hardware_group)
            )
            """;

    private final JdbcClient jdbc;
    private final SoftwareReadRepository software;

    @Override
    public Page<FoundRelease> find(ReleaseSearchFilter filter, Pageable pageable) {
        Map<String, Object> params = new HashMap<>();
        String where = where(filter, params);
        long total = jdbc.sql("WITH " + HARDWARE + " SELECT count(*) FROM software_release r"
                        + " JOIN software s ON s.id = r.software_id" + where)
                .params(params)
                .query(Long.class)
                .single();
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());
        List<FoundRelease> found = jdbc.sql("WITH " + HARDWARE
                        + """
                        SELECT r.id, r.software_id, s.title AS software_title, s.platform_code, r.title, r.version,
                               r.type, %s AS release_date, r.archive_file_name, r.archive_size,
                               ARRAY(SELECT rh.code FROM release_hardware rh WHERE rh.release_id = r.id
                                     ORDER BY rh.code) AS hardware
                        FROM software_release r
                        JOIN software s ON s.id = r.software_id
                        """.formatted(ReleaseDateSql.of("r"))
                        + where
                        + " ORDER BY lower(s.title), s.id, " + ReleaseDateSql.order("r") + ", r.id"
                        + " LIMIT :limit OFFSET :offset")
                .params(params)
                .query(this::release)
                .list();
        return new PageImpl<>(found, pageable, total);
    }

    private FoundRelease release(ResultSet rs, int row) throws SQLException {
        UUID id = rs.getObject("id", UUID.class);
        long size = rs.getLong("archive_size");
        return new FoundRelease(
                id,
                rs.getObject("software_id", UUID.class),
                rs.getString("software_title"),
                rs.getString("platform_code"),
                rs.getString("title"),
                rs.getString("version"),
                rs.getString("type"),
                rs.getString("release_date"),
                SqlArrays.list(rs.getArray("hardware"), String.class),
                rs.getString("archive_file_name"),
                rs.wasNull() ? null : size,
                software.findReleaseCredits(id));
    }

    private static String where(ReleaseSearchFilter f, Map<String, Object> params) {
        List<String> conditions = new ArrayList<>();
        if (f.word() != null) {
            params.put("word", f.word());
            conditions.add("(strpos(lower(COALESCE(r.title, '')), lower(:word)) > 0"
                    + " OR strpos(lower(COALESCE(r.version, '')), lower(:word)) > 0"
                    + " OR strpos(lower(s.title), lower(:word)) > 0)");
        }
        if (!f.software().isEmpty()) {
            params.put("software", f.software());
            conditions.add("r.software_id IN (:software)");
        }
        if (!f.types().isEmpty()) {
            params.put("types", f.types());
            conditions.add("r.type IN (:types)");
        }
        if (f.yearFrom() != null) {
            params.put("yearFrom", f.yearFrom());
            conditions.add("r.release_year >= :yearFrom");
        }
        if (f.yearTo() != null) {
            params.put("yearTo", f.yearTo());
            conditions.add("r.release_year <= :yearTo");
        }
        if (f.platform() != null) {
            params.put("platform", f.platform());
            conditions.add("s.platform_code = :platform");
        }
        if (!f.hardware().isEmpty()) {
            params.put("hardware", f.hardware());
            conditions.add("EXISTS (SELECT 1 FROM release_hardware rh WHERE rh.release_id = r.id"
                    + " AND rh.code IN (:hardware))");
        }
        if (!f.credited().isEmpty() || !f.roles().isEmpty()) {
            conditions.add(credited(f, params));
        }
        if (f.fileName() != null) {
            params.put("fileName", f.fileName());
            conditions.add("(strpos(lower(COALESCE(r.archive_file_name, '')), lower(:fileName)) > 0"
                    + " OR EXISTS (SELECT 1 FROM release_file rf WHERE rf.release_id = r.id"
                    + " AND strpos(lower(rf.file_name), lower(:fileName)) > 0))");
        }
        if (f.md5() != null) {
            params.put("md5", f.md5());
            conditions.add("EXISTS (SELECT 1 FROM release_file rf WHERE rf.release_id = r.id AND rf.md5 = :md5)");
        }
        return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    }

    /** A credit of the release by one of the credited, or under a label of theirs, in one of the roles. */
    private static String credited(ReleaseSearchFilter f, Map<String, Object> params) {
        List<String> credit = new ArrayList<>();
        if (!f.credited().isEmpty()) {
            params.put("credited", f.credited());
            credit.add("(c.person_id IN (:credited) OR c.group_id IN (:credited) OR c.label_id IN (:credited)"
                    + " OR l.person_id IN (:credited) OR l.group_id IN (:credited))");
        }
        if (!f.roles().isEmpty()) {
            params.put("roles", f.roles());
            credit.add("c.role_codes && ARRAY[:roles]::text[]");
        }
        return "EXISTS (SELECT 1 FROM software_release_contribution c LEFT JOIN label l ON l.id = c.label_id"
                + " WHERE c.release_id = r.id AND " + String.join(" AND ", credit) + ")";
    }
}
