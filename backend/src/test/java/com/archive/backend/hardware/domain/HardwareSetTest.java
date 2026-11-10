package com.archive.backend.hardware.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class HardwareSetTest {

    private static final PlatformCode VECTOR = new PlatformCode("vector");
    private static final PlatformCode BK = new PlatformCode("bk");
    private static final HardwareCode AY = new HardwareCode("ay8910");

    @Test
    void belongsToItsOwnPlatformOnly() {
        HardwareSet set = HardwareSet.restore(VECTOR, Set.of(AY));

        assertThat(set.belongsTo(VECTOR)).isTrue();
        assertThat(set.belongsTo(BK)).isFalse();
    }

    @Test
    void anEmptySetBelongsToAnyPlatform() {
        HardwareSet none = HardwareSet.restore(VECTOR, Set.of());

        assertThat(none.isEmpty()).isTrue();
        assertThat(none.belongsTo(BK)).isTrue();
    }

    @Test
    void isComparedByPlatformAndCodes() {
        assertThat(HardwareSet.restore(VECTOR, Set.of(AY))).isEqualTo(HardwareSet.restore(VECTOR, Set.of(AY)));
        assertThat(HardwareSet.restore(VECTOR, Set.of(AY))).isNotEqualTo(HardwareSet.restore(BK, Set.of(AY)));
        assertThat(HardwareSet.restore(VECTOR, Set.of(AY))).isNotEqualTo(HardwareSet.restore(VECTOR, Set.of()));
    }
}
