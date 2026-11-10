package com.archive.backend.hardware.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HardwareSetsTest {

    private static final PlatformCode VECTOR = new PlatformCode("vector");
    private static final PlatformCode BK = new PlatformCode("bk");
    private static final HardwareCode AY = new HardwareCode("ay8910");
    private static final HardwareCode BK0011M = new HardwareCode("bk0011m");

    /** Knows Vector with the AY chip and BK with the BK-0011M. */
    private static final HardwareCatalog CATALOG = new HardwareCatalog() {
        private final Map<PlatformCode, Set<HardwareCode>> hardware = Map.of(VECTOR, Set.of(AY), BK, Set.of(BK0011M));

        @Override
        public boolean platformExists(PlatformCode platform) {
            return hardware.containsKey(platform);
        }

        @Override
        public boolean allOnPlatform(Set<HardwareCode> codes, PlatformCode platform) {
            return hardware.getOrDefault(platform, Set.of()).containsAll(codes);
        }
    };

    private final HardwareSets sets = new HardwareSets(CATALOG);

    @Test
    void buildsASetOfHardwareOfThePlatform() {
        HardwareSet set = sets.of(VECTOR, Set.of(AY));

        assertThat(set.platform()).isEqualTo(VECTOR);
        assertThat(set.codes()).containsExactly(AY);
    }

    @Test
    void buildsAnEmptySetOfAKnownPlatform() {
        assertThat(sets.of(BK, Set.of()).isEmpty()).isTrue();
    }

    @Test
    void refusesAnUnknownPlatform() {
        assertThatThrownBy(() -> sets.of(new PlatformCode("zx"), Set.of()))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("software.platform-unknown"));
    }

    @Test
    void refusesHardwareOfAnotherPlatform() {
        assertThatThrownBy(() -> sets.of(VECTOR, Set.of(AY, BK0011M)))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("hardware.unknown");
                    assertThat(e.field()).isEqualTo("hardware");
                });
    }
}
