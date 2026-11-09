package com.archive.backend.software.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwarePlatform;
import com.archive.backend.software.read.SoftwareSummary;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class ListSoftwareHandlerTest {

    private final InMemorySoftwareReadRepository reads = new InMemorySoftwareReadRepository();
    private final ListSoftwareHandler handler = new ListSoftwareHandler(reads);

    @Test
    void returnsTheRequestedPageOfSoftware() {
        reads.add(details("First"));
        reads.add(details("Second"));
        reads.add(details("Third"));

        Page<SoftwareSummary> page = handler.handle(new ListSoftwareQuery(null, PageRequest.of(1, 2)));

        assertThat(page.getContent()).extracting(SoftwareSummary::title).containsExactly("Third");
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void asksForTheGivenPlatform() {
        handler.handle(new ListSoftwareQuery(new PlatformCode("bk"), PageRequest.of(0, 20)));

        assertThat(reads.lastPlatform()).isEqualTo("bk");
    }

    private static SoftwareDetails details(String title) {
        return new SoftwareDetails(
                UUID.randomUUID(),
                title,
                null,
                LocalDate.of(1990, 1, 1),
                new SoftwarePlatform("vector", "Vector"),
                List.of());
    }
}
