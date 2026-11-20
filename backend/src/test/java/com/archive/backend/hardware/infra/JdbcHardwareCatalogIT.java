package com.archive.backend.hardware.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/** Runs against the reference book of the migrations. */
@IntegrationTest
@Transactional
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
        assertThat(catalog.allOnPlatform(Set.of(new HardwareCode("tape")), new PlatformCode("bk")))
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
