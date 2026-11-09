package com.archive.backend.software.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareSummary;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class ListLatestSoftwareHandlerTest {

    private final InMemorySoftwareReadRepository reads = new InMemorySoftwareReadRepository();
    private final ListLatestSoftwareHandler handler = new ListLatestSoftwareHandler(reads);

    @Test
    void returnsTheMostRecentlyAddedSoftwareFirst() {
        reads.add(details("First"));
        reads.add(details("Second"));
        reads.add(details("Third"));

        assertThat(handler.handle(new ListLatestSoftwareQuery(PageRequest.of(0, 2)))
                        .getContent())
                .extracting(SoftwareSummary::title)
                .containsExactly("Third", "Second");
    }

    private static SoftwareDetails details(String title) {
        return new SoftwareDetails(UUID.randomUUID(), title, null, null);
    }
}
