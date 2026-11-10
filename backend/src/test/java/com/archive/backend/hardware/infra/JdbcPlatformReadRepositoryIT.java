package com.archive.backend.hardware.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.hardware.read.PlatformItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

/** The platforms are reference data: the schema itself holds them. */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcPlatformReadRepository.class})
class JdbcPlatformReadRepositoryIT {

    @Autowired
    private JdbcPlatformReadRepository reads;

    @Test
    void readsTheStartingPlatformsByName() {
        assertThat(reads.findAll())
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
}
