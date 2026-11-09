package com.archive.backend.software.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeleteSoftwareHandlerTest {

    private static final SoftwareId ID = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlatformCode VECTOR = new PlatformCode("vector");

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository();
    private final DeleteSoftwareHandler handler = new DeleteSoftwareHandler(repository);

    @Test
    void removesTheSoftware() {
        repository.save(Software.create(ID, new SoftwareTitle("Elite"), null, null, VECTOR));

        handler.handle(new DeleteSoftwareCommand(ID));

        assertThat(repository.findById(ID)).isEmpty();
    }

    @Test
    void failsWhenTheSoftwareDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new DeleteSoftwareCommand(ID)))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }
}
