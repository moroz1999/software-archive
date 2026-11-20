package com.archive.backend.hardware.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.hardware.read.PlatformItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** The platforms are reference data: the schema itself holds them. */
@IntegrationTest
@Transactional
class JdbcPlatformReadRepositoryIT {

    @Autowired
    private JdbcPlatformReadRepository reads;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void readsTheStartingPlatformsByName() {
        assertThat(reads.findAll("en"))
                .containsExactly(
                        new PlatformItem("agat", "Agat"),
                        new PlatformItem("bashkiria", "Bashkiria"),
                        new PlatformItem("bk", "BK"),
                        new PlatformItem("korvet", "Korvet"),
                        new PlatformItem("lviv", "Lviv"),
                        new PlatformItem("msx", "MSX"),
                        new PlatformItem("okean", "Okean"),
                        new PlatformItem("orion", "Orion"),
                        new PlatformItem("pk8000", "PK8000"),
                        new PlatformItem("rk", "RK-compatible"),
                        new PlatformItem("specialist", "Specialist"),
                        new PlatformItem("vector", "Vector"),
                        new PlatformItem("xt", "XT clones"));
    }

    @Test
    void readsThePlatformsByNameInRussian() {
        assertThat(reads.findAll("ru"))
                .containsExactly(
                        new PlatformItem("agat", "Агат"),
                        new PlatformItem("bashkiria", "Башкирия"),
                        new PlatformItem("bk", "БК"),
                        new PlatformItem("vector", "Вектор"),
                        new PlatformItem("xt", "Клоны XT"),
                        new PlatformItem("korvet", "Корвет"),
                        new PlatformItem("lviv", "Львов"),
                        new PlatformItem("okean", "Океан"),
                        new PlatformItem("orion", "Орион"),
                        new PlatformItem("pk8000", "ПК8000"),
                        new PlatformItem("rk", "РК-совместимые"),
                        new PlatformItem("specialist", "Специалист"),
                        new PlatformItem("msx", "Ямаха КУВТ"));
    }

    @Test
    void namesAPlatformWithoutANameInTheLanguageAskedForInRussian() {
        jdbc.update("INSERT INTO platform (code) VALUES ('zx')");
        jdbc.update("INSERT INTO platform_name (platform_code, language, name) VALUES ('zx', 'ru', 'Спектрум')");

        assertThat(reads.findAll("en")).last().isEqualTo(new PlatformItem("zx", "Спектрум"));
    }
}
