package com.archive.backend.software.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CreateSoftwareHandlerTest {

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository();
    private final CreateSoftwareHandler handler = new CreateSoftwareHandler(repository);

    @Test
    void storesNewSoftwareUnderANewId() {
        SoftwareId id = handler.handle(new CreateSoftwareCommand(
                new SoftwareTitle("Elite"), new SoftwareDescription("Space trading"), LocalDate.of(1985, 9, 20)));

        assertThat(repository.findById(id)).hasValueSatisfying(software -> {
            assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite"));
            assertThat(software.getDescription()).isEqualTo(new SoftwareDescription("Space trading"));
            assertThat(software.getReleaseDate()).isEqualTo(LocalDate.of(1985, 9, 20));
        });
    }

    @Test
    void givesEverySoftwareItsOwnId() {
        SoftwareId first = handler.handle(new CreateSoftwareCommand(new SoftwareTitle("Elite"), null, null));
        SoftwareId second = handler.handle(new CreateSoftwareCommand(new SoftwareTitle("Elite"), null, null));

        assertThat(first).isNotEqualTo(second);
    }
}
