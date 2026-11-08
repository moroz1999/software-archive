package com.archive.backend.software.app;

import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareSummary;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ListSoftwareHandlerTest {

    private final InMemorySoftwareReadRepository reads = new InMemorySoftwareReadRepository();
    private final ListSoftwareHandler handler = new ListSoftwareHandler(reads);

    @Test
    void returnsTheRequestedPageOfSoftware() {
        reads.add(details("First"));
        reads.add(details("Second"));
        reads.add(details("Third"));

        Page<SoftwareSummary> page = handler.handle(new ListSoftwareQuery(PageRequest.of(1, 2)));

        assertThat(page.getContent()).extracting(SoftwareSummary::title).containsExactly("Third");
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    private static SoftwareDetails details(String title) {
        return new SoftwareDetails(UUID.randomUUID(), title, null, LocalDate.of(1990, 1, 1));
    }
}
