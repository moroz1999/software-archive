package com.archive.backend.hardware.infra;

import com.archive.backend.hardware.domain.HardwareCatalog;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcHardwareCatalog implements HardwareCatalog {

    private final JdbcClient jdbc;

    @Override
    public boolean platformExists(PlatformCode platform) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM platform WHERE code = :code)")
                .param("code", platform.value())
                .query(Boolean.class)
                .single();
    }

    @Override
    public boolean allOnPlatform(Set<HardwareCode> codes, PlatformCode platform) {
        if (codes.isEmpty()) {
            return true;
        }
        List<String> values = codes.stream().map(HardwareCode::value).toList();
        int known = jdbc.sql("""
                        SELECT count(*) FROM hardware_platform
                        WHERE platform_code = :platform AND hardware_code IN (:codes)
                        """)
                .param("platform", platform.value())
                .param("codes", values)
                .query(Integer.class)
                .single();
        return known == values.size();
    }
}
