package com.archive.backend.hardware.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

/** Runs against the reference book of the migrations. */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcHardwareCatalog.class})
class JdbcHardwareCatalogIT {

    private static final PlatformCode VECTOR = new PlatformCode("vector");

    @Autowired
    private JdbcHardwareCatalog catalog;

    @Test
    void knowsAPlatformOfTheArchive() {
        assertThat(catalog.platformExists(VECTOR)).isTrue();
    }

    @Test
    void doesNotKnowAnyOtherPlatform() {
        assertThat(catalog.platformExists(new PlatformCode("zx"))).isFalse();
    }

    @Test
    void knowsHardwareOfThePlatformIncludingItemsOfSeveralPlatforms() {
        assertThat(catalog.allOnPlatform(
                        Set.of(new HardwareCode("vector06c"), new HardwareCode("ay8910"), new HardwareCode("tape")),
                        VECTOR))
                .isTrue();
    }

    @Test
    void refusesHardwareOfAnotherPlatform() {
        assertThat(catalog.allOnPlatform(Set.of(new HardwareCode("ay8910"), new HardwareCode("bk0011m")), VECTOR))
                .isFalse();
    }

    @Test
    void refusesHardwareOutsideTheReferenceBook() {
        assertThat(catalog.allOnPlatform(Set.of(new HardwareCode("gus")), VECTOR))
                .isFalse();
    }

    @Test
    void acceptsNoHardware() {
        assertThat(catalog.allOnPlatform(Set.of(), VECTOR)).isTrue();
    }
}
