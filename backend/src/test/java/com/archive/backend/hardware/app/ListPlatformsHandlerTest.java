package com.archive.backend.hardware.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.hardware.read.PlatformItem;
import org.junit.jupiter.api.Test;

class ListPlatformsHandlerTest {

    private final InMemoryPlatformReadRepository reads = new InMemoryPlatformReadRepository();
    private final ListPlatformsHandler handler = new ListPlatformsHandler(reads);

    @Test
    void returnsThePlatforms() {
        reads.add(new PlatformItem("agat", "Agat"));
        reads.add(new PlatformItem("bk", "BK"));

        assertThat(handler.handle(new ListPlatformsQuery()))
                .containsExactly(new PlatformItem("agat", "Agat"), new PlatformItem("bk", "BK"));
    }
}
