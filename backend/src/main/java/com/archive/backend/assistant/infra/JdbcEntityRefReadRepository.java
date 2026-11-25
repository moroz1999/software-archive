package com.archive.backend.assistant.infra;

import com.archive.backend.assistant.read.chat.EntityRef;
import com.archive.backend.assistant.read.chat.EntityRefReadRepository;
import com.archive.backend.geo.read.GeoNameSql;
import com.archive.backend.hardware.read.HardwareNameSql;
import com.archive.backend.label.read.MainNameSql;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Entries as links, by their ids, in Russian, the language every name has. A release is shown on the page of its
 * software; countries, cities and hardware on the pages that keep them.
 */
@Repository
@RequiredArgsConstructor
public class JdbcEntityRefReadRepository implements EntityRefReadRepository {

    private final JdbcClient jdbc;

    @Override
    public List<EntityRef> byIds(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jdbc.sql("WITH " + MainNameSql.PEOPLE + ", " + MainNameSql.GROUPS
                        + """
                        SELECT 'software' AS type, s.id::text AS id, s.title AS title, '/software/' || s.id AS path
                        FROM software s WHERE s.id IN (:ids)
                        UNION ALL
                        SELECT 'release', r.id::text,
                            trim(COALESCE(r.title, s.title) || ' ' || COALESCE(r.version, '')),
                            '/software/' || s.id
                        FROM software_release r JOIN software s ON s.id = r.software_id WHERE r.id IN (:ids)
                        UNION ALL
                        SELECT 'author', p.person_id::text, p.main_name, '/authors/' || p.person_id
                        FROM named_person p WHERE p.person_id IN (:ids)
                        UNION ALL
                        SELECT 'group', g.group_id::text, g.main_name, '/groups/' || g.group_id
                        FROM named_group g WHERE g.group_id IN (:ids)
                        UNION ALL
                        SELECT 'label', l.id::text, l.name, '/labels/' || l.id FROM label l WHERE l.id IN (:ids)
                        UNION ALL
                        SELECT 'country', c.id::text, %s, '/management/geography' FROM country c WHERE c.id IN (:ids)
                        UNION ALL
                        SELECT 'city', c.id::text, %s, '/management/geography' FROM city c WHERE c.id IN (:ids)
                        """.formatted(GeoNameSql.country("c.id"), GeoNameSql.city("c.id")))
                .param("ids", ids)
                .param("language", "ru")
                .query((rs, n) -> new EntityRef(
                        rs.getString("type"), rs.getString("id"), rs.getString("title"), rs.getString("path")))
                .list();
    }

    @Override
    public List<EntityRef> hardware(Collection<String> codes) {
        if (codes.isEmpty()) {
            return List.of();
        }
        return jdbc.sql("SELECT h.code, " + HardwareNameSql.hardware("h.code")
                        + " AS name FROM hardware h WHERE h.code IN (:codes)")
                .param("codes", codes)
                .param("language", "ru")
                .query((rs, n) -> new EntityRef(
                        "hardware",
                        rs.getString("code"),
                        rs.getString("name"),
                        "/management/hardware/" + rs.getString("code") + "/edit"))
                .list();
    }
}
