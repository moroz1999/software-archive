package com.archive.backend.hardware.infra;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.HardwareGroupCode;
import com.archive.backend.hardware.domain.HardwareUses;
import com.archive.backend.hardware.domain.PlatformCode;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcHardwareUses implements HardwareUses {

    private final JdbcClient jdbc;

    @Override
    public boolean inUse(HardwareCode hardware) {
        return jdbc.sql("""
                        SELECT EXISTS (SELECT 1 FROM software_hardware WHERE hardware_code = :code)
                            OR EXISTS (SELECT 1 FROM software_release_hardware WHERE hardware_code = :code)
                        """)
                .param("code", hardware.value())
                .query(Boolean.class)
                .single();
    }

    @Override
    public boolean usedOn(HardwareCode hardware, Set<PlatformCode> platforms) {
        if (platforms.isEmpty()) {
            return false;
        }
        return jdbc.sql("""
                        SELECT EXISTS (SELECT 1 FROM software s
                                       WHERE s.platform_code IN (:platforms)
                                         AND (EXISTS (SELECT 1 FROM software_hardware sh
                                                      WHERE sh.software_id = s.id AND sh.hardware_code = :code)
                                              OR EXISTS (SELECT 1 FROM software_release r
                                                         JOIN software_release_hardware rh ON rh.release_id = r.id
                                                         WHERE r.software_id = s.id AND rh.hardware_code = :code)))
                        """)
                .param("code", hardware.value())
                .param("platforms", platforms.stream().map(PlatformCode::value).toList())
                .query(Boolean.class)
                .single();
    }

    @Override
    public boolean groupInUse(HardwareGroupCode group) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM hardware WHERE hardware_group = :code)")
                .param("code", group.value())
                .query(Boolean.class)
                .single();
    }
}
