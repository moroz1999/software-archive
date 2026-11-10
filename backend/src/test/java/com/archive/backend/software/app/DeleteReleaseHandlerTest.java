package com.archive.backend.software.app;

import static com.archive.backend.software.app.ReleaseFixtures.AY_MOD;
import static com.archive.backend.software.app.ReleaseFixtures.ELITE;
import static com.archive.backend.software.app.ReleaseFixtures.UNKNOWN;
import static com.archive.backend.software.app.ReleaseFixtures.eliteWithAyMod;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.NotFoundException;
import org.junit.jupiter.api.Test;

class DeleteReleaseHandlerTest {

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository();
    private final DeleteReleaseHandler handler = new DeleteReleaseHandler(repository);

    @Test
    void removesTheReleaseFromItsSoftware() {
        repository.save(eliteWithAyMod());

        handler.handle(new DeleteReleaseCommand(ELITE, AY_MOD));

        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(
                        software -> assertThat(software.getReleases()).isEmpty());
    }

    @Test
    void failsWhenTheSoftwareDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new DeleteReleaseCommand(ELITE, AY_MOD)))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }

    @Test
    void failsWhenTheSoftwareHasNoSuchRelease() {
        repository.save(eliteWithAyMod());

        assertThatThrownBy(() -> handler.handle(new DeleteReleaseCommand(ELITE, UNKNOWN)))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("release.not-found"));
    }
}
