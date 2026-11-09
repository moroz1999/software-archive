package com.archive.backend.software.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UpdateSoftwareHandlerTest {

    private static final SoftwareId ID = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlatformCode VECTOR = new PlatformCode("vector");
    private static final PlatformCode BK = new PlatformCode("bk");

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository();
    private final UpdateSoftwareHandler handler =
            new UpdateSoftwareHandler(repository, new InMemoryPlatformRepository(VECTOR, BK));

    @Test
    void storesTheEditedSoftware() {
        repository.save(Software.create(ID, new SoftwareTitle("Elite"), null, null, VECTOR));

        handler.handle(new UpdateSoftwareCommand(
                ID,
                new SoftwareTitle("Elite Plus"),
                new SoftwareDescription("Enhanced"),
                LocalDate.of(1987, 1, 1),
                BK));

        assertThat(repository.findById(ID)).hasValueSatisfying(software -> {
            assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite Plus"));
            assertThat(software.getDescription()).isEqualTo(new SoftwareDescription("Enhanced"));
            assertThat(software.getReleaseDate()).isEqualTo(LocalDate.of(1987, 1, 1));
            assertThat(software.getPlatform()).isEqualTo(BK);
        });
    }

    @Test
    void failsWhenTheSoftwareDoesNotExist() {
        assertThatThrownBy(() ->
                        handler.handle(new UpdateSoftwareCommand(ID, new SoftwareTitle("Elite"), null, null, VECTOR)))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }

    @Test
    void refusesAnUnknownPlatformAndKeepsTheSoftware() {
        repository.save(Software.create(ID, new SoftwareTitle("Elite"), null, null, VECTOR));

        assertThatThrownBy(() -> handler.handle(new UpdateSoftwareCommand(
                        ID, new SoftwareTitle("Elite Plus"), null, null, new PlatformCode("zx"))))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("software.platform-unknown"));
        assertThat(repository.findById(ID))
                .hasValueSatisfying(
                        software -> assertThat(software.getPlatform()).isEqualTo(VECTOR));
    }
}
