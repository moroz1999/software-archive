package com.archive.backend.software.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CreateSoftwareHandlerTest {

    private static final PlatformCode VECTOR = new PlatformCode("vector");

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository();
    private final CreateSoftwareHandler handler =
            new CreateSoftwareHandler(repository, new InMemoryPlatformRepository(VECTOR));

    @Test
    void storesNewSoftwareUnderANewId() {
        SoftwareId id = handler.handle(new CreateSoftwareCommand(
                new SoftwareTitle("Elite"),
                new SoftwareDescription("Space trading"),
                LocalDate.of(1985, 9, 20),
                VECTOR));

        assertThat(repository.findById(id)).hasValueSatisfying(software -> {
            assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite"));
            assertThat(software.getDescription()).isEqualTo(new SoftwareDescription("Space trading"));
            assertThat(software.getReleaseDate()).isEqualTo(LocalDate.of(1985, 9, 20));
            assertThat(software.getPlatform()).isEqualTo(VECTOR);
        });
    }

    @Test
    void givesEverySoftwareItsOwnId() {
        SoftwareId first = handler.handle(new CreateSoftwareCommand(new SoftwareTitle("Elite"), null, null, VECTOR));
        SoftwareId second = handler.handle(new CreateSoftwareCommand(new SoftwareTitle("Elite"), null, null, VECTOR));

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void refusesAnUnknownPlatformAndStoresNothing() {
        assertThatThrownBy(() -> handler.handle(
                        new CreateSoftwareCommand(new SoftwareTitle("Elite"), null, null, new PlatformCode("zx"))))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("software.platform-unknown"));
        assertThat(repository.count()).isZero();
    }
}
