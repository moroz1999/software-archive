package com.archive.backend.software.app;

import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateSoftwareHandlerTest {

    private static final SoftwareId ID = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository();
    private final UpdateSoftwareHandler handler = new UpdateSoftwareHandler(repository);

    @Test
    void storesTheEditedSoftware() {
        repository.save(Software.create(ID, new SoftwareTitle("Elite"), null, null));

        handler.handle(new UpdateSoftwareCommand(ID, new SoftwareTitle("Elite Plus"), new SoftwareDescription("Enhanced"), LocalDate.of(1987, 1, 1)));

        assertThat(repository.findById(ID)).hasValueSatisfying(software -> {
            assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite Plus"));
            assertThat(software.getDescription()).isEqualTo(new SoftwareDescription("Enhanced"));
            assertThat(software.getReleaseDate()).isEqualTo(LocalDate.of(1987, 1, 1));
        });
    }

    @Test
    void failsWhenTheSoftwareDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new UpdateSoftwareCommand(ID, new SoftwareTitle("Elite"), null, null)))
                .isInstanceOfSatisfying(NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }
}
