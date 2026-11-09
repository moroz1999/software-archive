package com.archive.backend.hardware.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.hardware.domain.PlatformCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcPlatformRepository.class})
class JdbcPlatformRepositoryIT {

    @Autowired
    private JdbcPlatformRepository platforms;

    @Test
    void knowsAPlatformOfTheArchive() {
        assertThat(platforms.exists(new PlatformCode("vector"))).isTrue();
    }

    @Test
    void doesNotKnowAnyOtherPlatform() {
        assertThat(platforms.exists(new PlatformCode("zx"))).isFalse();
    }
}
