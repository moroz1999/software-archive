package com.archive.backend.hardware.infra;

import com.archive.backend.hardware.read.HardwareDetails;
import com.archive.backend.hardware.read.HardwareGroupDetails;
import com.archive.backend.hardware.read.HardwareGroupItem;
import com.archive.backend.hardware.read.HardwareItem;
import com.archive.backend.hardware.read.HardwareNameSql;
import com.archive.backend.hardware.read.HardwareReadRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcHardwareReadRepository implements HardwareReadRepository {

    /** The hardware {@code hw} where {@code %s} holds, named, with its group named, in the order it is shown. */
    private static final String ITEMS =
            """
            SELECT h.code, h.name, h.hardware_group, h.group_name
            FROM (SELECT hw.code, %s AS name, hw.hardware_group, %s AS group_name, g.position
                  FROM hardware hw
                  JOIN hardware_group g ON g.code = hw.hardware_group
                  WHERE %%s) h
            ORDER BY h.position, lower(h.name), h.code
            """.formatted(HardwareNameSql.hardware("hw.code"), HardwareNameSql.group("g.code"));

    private final JdbcClient jdbc;

    @Override
    public List<HardwareItem> findByPlatform(String platform, String language) {
        return jdbc.sql(ITEMS.formatted(
                        "hw.code IN (SELECT hardware_code FROM hardware_platform WHERE platform_code = :platform)"))
                .param("platform", platform)
                .param("language", language)
                .query((rs, row) -> new HardwareItem(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("hardware_group"),
                        rs.getString("group_name")))
                .list();
    }

    @Override
    public List<HardwareItem> findByCodes(Collection<String> codes, String language) {
        if (codes.isEmpty()) {
            return List.of();
        }
        return jdbc.sql(ITEMS.formatted("hw.code IN (:codes)"))
                .param("codes", List.copyOf(codes))
                .param("language", language)
                .query((rs, row) -> new HardwareItem(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("hardware_group"),
                        rs.getString("group_name")))
                .list();
    }

    @Override
    public List<HardwareGroupItem> findGroups(String language) {
        Map<String, String> groups = new LinkedHashMap<>();
        jdbc.sql("SELECT g.code, " + HardwareNameSql.group("g.code")
                        + " AS name FROM hardware_group g ORDER BY g.position")
                .param("language", language)
                .query((rs, row) -> groups.put(rs.getString("code"), rs.getString("name")))
                .list();
        Map<String, List<HardwareGroupItem.Entry>> hardware = new HashMap<>();
        jdbc.sql("""
                        SELECT h.code, h.name, h.hardware_group
                        FROM (SELECT hw.code, %s AS name, hw.hardware_group FROM hardware hw) h
                        ORDER BY lower(h.name), h.code
                        """.formatted(HardwareNameSql.hardware("hw.code")))
                .param("language", language)
                .query((rs, row) -> hardware.computeIfAbsent(rs.getString("hardware_group"), key -> new ArrayList<>())
                        .add(new HardwareGroupItem.Entry(rs.getString("code"), rs.getString("name"))))
                .list();
        return groups.entrySet().stream()
                .map(group -> new HardwareGroupItem(
                        group.getKey(), group.getValue(), hardware.getOrDefault(group.getKey(), List.of())))
                .toList();
    }

    @Override
    public Optional<HardwareGroupDetails> findGroup(String code) {
        return jdbc.sql("SELECT code FROM hardware_group WHERE code = :code")
                .param("code", code)
                .query(String.class)
                .optional()
                .map(found -> new HardwareGroupDetails(found, names("hardware_group_name", "group_code", found)));
    }

    @Override
    public Optional<HardwareDetails> findHardware(String code) {
        return jdbc.sql("SELECT code, hardware_group FROM hardware WHERE code = :code")
                .param("code", code)
                .query((rs, row) -> new HardwareDetails(
                        rs.getString("code"),
                        names("hardware_name", "hardware_code", code),
                        rs.getString("hardware_group"),
                        jdbc.sql("SELECT platform_code FROM hardware_platform WHERE hardware_code = :code"
                                        + " ORDER BY platform_code")
                                .param("code", code)
                                .query(String.class)
                                .list()))
                .optional();
    }

    private Map<String, String> names(String table, String ownerColumn, String owner) {
        Map<String, String> names = new HashMap<>();
        jdbc.sql("SELECT language, name FROM " + table + " WHERE " + ownerColumn + " = :code")
                .param("code", owner)
                .query((rs, row) -> names.put(rs.getString("language"), rs.getString("name")))
                .list();
        return names;
    }
}
