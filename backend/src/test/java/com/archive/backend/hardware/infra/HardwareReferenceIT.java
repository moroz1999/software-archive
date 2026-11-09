package com.archive.backend.hardware.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/** The hardware reference book is reference data: the schema itself holds it (docs/domain/hardware.md). */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class HardwareReferenceIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void holdsHardwareOfEveryGroup() {
        assertThat(jdbc.queryForList("SELECT DISTINCT hardware_group FROM hardware ORDER BY 1", String.class))
                .containsExactly("computers", "controls", "dos", "expansion", "sound", "storage");
    }

    @Test
    void letsOneItemBelongToSeveralPlatforms() {
        assertThat(jdbc.queryForList(
                        "SELECT platform_code FROM hardware_platform WHERE hardware_code = 'ay8910' ORDER BY 1",
                        String.class))
                .containsExactly("msx", "specialist", "vector");
    }

    @Test
    void holdsTheModelsAndClonesOfAPlatformAsItsComputers() {
        assertThat(jdbc.queryForList("""
                        SELECT h.code FROM hardware h
                        JOIN hardware_platform p ON p.hardware_code = h.code
                        WHERE h.hardware_group = 'computers' AND p.platform_code = 'bk'
                        ORDER BY 1
                        """, String.class)).containsExactly("bk0010-01", "bk0011m");
    }
}
