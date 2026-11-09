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
                        new PlatformItem("bk", "BK"),
                        new PlatformItem("msx", "MSX"),
                        new PlatformItem("specialist", "Specialist"),
                        new PlatformItem("vector", "Vector"));
    }
}
