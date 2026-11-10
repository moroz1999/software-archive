package com.archive.backend.hardware.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.hardware.read.HardwareItem;
import com.archive.backend.hardware.read.HardwareReadRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ListHardwareHandlerTest {

    private static final HardwareItem VECTOR06C = new HardwareItem("vector06c", "Vector-06C", "computers");

    private final HardwareReadRepository reads =
            platform -> Map.of("vector", List.of(VECTOR06C)).getOrDefault(platform, List.of());

    @Test
    void returnsTheHardwareOfThePlatform() {
        assertThat(new ListHardwareHandler(reads).handle(new ListHardwareQuery(new PlatformCode("vector"))))
                .containsExactly(VECTOR06C);
    }
}
