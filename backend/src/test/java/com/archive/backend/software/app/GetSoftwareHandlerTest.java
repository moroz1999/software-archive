package com.archive.backend.software.app;

import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.read.SoftwareDetails;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetSoftwareHandlerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final InMemorySoftwareReadRepository reads = new InMemorySoftwareReadRepository();
    private final GetSoftwareHandler handler = new GetSoftwareHandler(reads);

    @Test
    void returnsTheSoftware() {
        SoftwareDetails details = new SoftwareDetails(ID, "Elite", "Space trading", LocalDate.of(1985, 9, 20));
        reads.add(details);

        assertThat(handler.handle(new GetSoftwareQuery(new SoftwareId(ID)))).isEqualTo(details);
    }

    @Test
    void failsWhenTheSoftwareDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new GetSoftwareQuery(new SoftwareId(ID))))
                .isInstanceOfSatisfying(NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }
}
