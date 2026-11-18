package com.archive.backend.hardware.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.hardware.read.PlatformItem;
import com.archive.backend.shared.domain.Language;
import org.junit.jupiter.api.Test;

class ListPlatformsHandlerTest {

    private final InMemoryPlatformReadRepository reads = new InMemoryPlatformReadRepository();
    private final ListPlatformsHandler handler = new ListPlatformsHandler(reads);

    @Test
    void returnsThePlatformsInTheLanguageAskedFor() {
        reads.add("ru", new PlatformItem("agat", "Агат"));
        reads.add("ru", new PlatformItem("bk", "БК"));
        reads.add("en", new PlatformItem("agat", "Agat"));

        assertThat(handler.handle(new ListPlatformsQuery(Language.RU)))
                .containsExactly(new PlatformItem("agat", "Агат"), new PlatformItem("bk", "БК"));
    }
}
