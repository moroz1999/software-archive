package com.archive.backend.hardware.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** The hardware reference book is reference data: the schema itself holds it (docs/domain/hardware.md). */
@IntegrationTest
@Transactional
class HardwareReferenceIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void holdsOnlyTheHardwareOfTheVectorAndBkFamilies() {
        assertThat(jdbc.queryForList(
                        "SELECT hardware_code || ':' || platform_code FROM hardware_platform ORDER BY 1", String.class))
                .containsExactlyInAnyOrder(
                        "azbk:bk",
                        "basic:bk",
                        "bk0010:bk",
                        "bk0010-01:bk",
                        "bk0011:bk",
                        "bk0011m:bk",
                        "focal:bk",
                        "smk512:bk",
                        "ay8910:vector",
                        "barkar:vector",
                        "beeper:vector",
                        "floppy:vector",
                        "hdd:vector",
                        "joystick-p:vector",
                        "joystick-s:vector",
                        "krista2:vector",
                        "kvaz:vector",
                        "pk6128c:vector",
                        "start1200:vector",
                        "tape:vector",
                        "vector06c:vector",
                        "vi53:vector",
                        "z80:vector");
        assertThat(jdbc.queryForList("SELECT code FROM hardware ORDER BY 1", String.class))
                .containsExactlyInAnyOrder(
                        "azbk",
                        "basic",
                        "bk0010",
                        "bk0010-01",
                        "bk0011",
                        "bk0011m",
                        "focal",
                        "smk512",
                        "ay8910",
                        "barkar",
                        "beeper",
                        "floppy",
                        "hdd",
                        "joystick-p",
                        "joystick-s",
                        "krista2",
                        "kvaz",
                        "pk6128c",
                        "start1200",
                        "tape",
                        "vector06c",
                        "vi53",
                        "z80");
    }

    @Test
    void holdsTheModelsAndClonesOfTheVectorFamilyAsItsComputers() {
        assertThat(jdbc.queryForList(
                        "SELECT h.code FROM hardware h JOIN hardware_platform p ON p.hardware_code = h.code"
                                + " WHERE p.platform_code = 'vector' AND h.hardware_group = 'computers' ORDER BY 1",
                        String.class))
                .containsExactly("krista2", "pk6128c", "start1200", "vector06c");
    }

    @Test
    void holdsTheModelsAndClonesOfTheBkFamilyAsItsComputersAndTheSmk512AsItsExpansion() {
        assertThat(jdbc.queryForList(
                        "SELECT h.code || ':' || h.hardware_group FROM hardware h"
                                + " JOIN hardware_platform p ON p.hardware_code = h.code"
                                + " WHERE p.platform_code = 'bk' AND h.hardware_group IN ('computers', 'expansion')",
                        String.class))
                .containsExactlyInAnyOrder(
                        "azbk:computers",
                        "bk0010:computers",
                        "bk0010-01:computers",
                        "bk0011:computers",
                        "bk0011m:computers",
                        "smk512:expansion");
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
