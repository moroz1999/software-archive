package com.archive.backend.software.app;

import static com.archive.backend.software.app.ReleaseFixtures.AY;
import static com.archive.backend.software.app.ReleaseFixtures.AY_MOD;
import static com.archive.backend.software.app.ReleaseFixtures.AY_MOD_CONTENT;
import static com.archive.backend.software.app.ReleaseFixtures.BK0011M;
import static com.archive.backend.software.app.ReleaseFixtures.BUGFIX_CONTENT;
import static com.archive.backend.software.app.ReleaseFixtures.ELITE;
import static com.archive.backend.software.app.ReleaseFixtures.UNKNOWN;
import static com.archive.backend.software.app.ReleaseFixtures.eliteWithAyMod;
import static com.archive.backend.software.app.ReleaseFixtures.hardwareSets;
import static com.archive.backend.software.app.ReleaseFixtures.vector;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UpdateReleaseHandlerTest {

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository();
    private final UpdateReleaseHandler handler = new UpdateReleaseHandler(repository, hardwareSets());

    @Test
    void storesTheEditedRelease() {
        repository.save(eliteWithAyMod());

        handler.handle(new UpdateReleaseCommand(ELITE, AY_MOD, BUGFIX_CONTENT, Set.of()));

        assertThat(repository.findById(ELITE).flatMap(software -> software.release(AY_MOD)))
                .hasValueSatisfying(release -> {
                    assertThat(release.content()).isEqualTo(BUGFIX_CONTENT);
                    assertThat(release.hardware()).isEqualTo(vector());
                });
    }

    @Test
    void failsWhenTheSoftwareDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new UpdateReleaseCommand(ELITE, AY_MOD, AY_MOD_CONTENT, Set.of(AY))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }

    @Test
    void failsWhenTheSoftwareHasNoSuchRelease() {
        repository.save(eliteWithAyMod());

        assertThatThrownBy(() -> handler.handle(new UpdateReleaseCommand(ELITE, UNKNOWN, AY_MOD_CONTENT, Set.of(AY))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("release.not-found"));
    }

    @Test
    void refusesHardwareOfAnotherPlatformAndKeepsTheRelease() {
        repository.save(eliteWithAyMod());

        assertThatThrownBy(() ->
                        handler.handle(new UpdateReleaseCommand(ELITE, AY_MOD, BUGFIX_CONTENT, Set.of(AY, BK0011M))))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("hardware.unknown"));
        assertThat(repository.findById(ELITE).flatMap(software -> software.release(AY_MOD)))
                .hasValueSatisfying(release -> {
                    assertThat(release.content()).isEqualTo(AY_MOD_CONTENT);
                    assertThat(release.hardware()).isEqualTo(vector(AY));
                });
    }
}
