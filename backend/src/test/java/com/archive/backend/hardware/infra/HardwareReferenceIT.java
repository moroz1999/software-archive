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
    void holdsOnlyTheConfirmedHardwareOfTheVectorFamily() {
        assertThat(jdbc.queryForList(
                        "SELECT hardware_code || ':' || platform_code FROM hardware_platform ORDER BY 1", String.class))
                .containsExactly("ay8910:vector", "floppy:vector", "krista2:vector", "tape:vector", "vector06c:vector");
        assertThat(jdbc.queryForList("SELECT code FROM hardware ORDER BY 1", String.class))
                .containsExactly("ay8910", "floppy", "krista2", "tape", "vector06c");
    }

    @Test
    void holdsTheModelsAndClonesOfTheVectorFamilyAsItsComputers() {
        assertThat(jdbc.queryForList(
                        "SELECT code FROM hardware WHERE hardware_group = 'computers' ORDER BY 1", String.class))
                .containsExactly("krista2", "vector06c");
    }

    @Test
    void keepsEveryPlatformOfThePlannedFamilies() {
        assertThat(jdbc.queryForList("SELECT code FROM platform ORDER BY 1", String.class))
                .containsExactly(
                        "agat",
                        "bashkiria",
                        "bk",
                        "korvet",
                        "lviv",
                        "msx",
                        "okean",
                        "orion",
                        "pk8000",
                        "rk",
                        "specialist",
                        "vector",
                        "xt");
    }

    @Test
    void namesEveryPlatformAndEveryItemInRussianAndInEnglish() {
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM platform p WHERE (SELECT count(*) FROM platform_name n"
                                + " WHERE n.platform_code = p.code AND n.language IN ('ru', 'en')) <> 2",
                        Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM hardware h WHERE (SELECT count(*) FROM hardware_name n"
                                + " WHERE n.hardware_code = h.code AND n.language IN ('ru', 'en')) <> 2",
                        Integer.class))
                .isZero();
        assertThat(jdbc.queryForList(
                        "SELECT language || ':' || name FROM hardware_name WHERE hardware_code = 'krista2'"
                                + " AND language IN ('ru', 'en') ORDER BY 1",
                        String.class))
                .containsExactly("en:Krista-2", "ru:Криста-2");
    }
}
