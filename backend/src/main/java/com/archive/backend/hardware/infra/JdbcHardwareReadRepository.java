package com.archive.backend.hardware.infra;

import com.archive.backend.hardware.read.HardwareItem;
import com.archive.backend.hardware.read.HardwareReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcHardwareReadRepository implements HardwareReadRepository {

    private final JdbcClient jdbc;

    @Override
    public List<HardwareItem> findByPlatform(String platform) {
        return jdbc.sql("""
                        SELECT h.code, h.name, h.hardware_group
                        FROM hardware h
                        JOIN hardware_platform hp ON hp.hardware_code = h.code
                        WHERE hp.platform_code = :platform
                        ORDER BY array_position(ARRAY['computers', 'storage', 'dos', 'sound', 'controls', 'expansion'],
                                 h.hardware_group), lower(h.name), h.code
                        """)
                .param("platform", platform)
                .query((rs, row) ->
                        new HardwareItem(rs.getString("code"), rs.getString("name"), rs.getString("hardware_group")))
                .list();
    }
}
