package com.archive.backend.software.app;

import static com.archive.backend.software.app.ReleaseFixtures.AY;
import static com.archive.backend.software.app.ReleaseFixtures.AY_MOD_CONTENT;
import static com.archive.backend.software.app.ReleaseFixtures.BK0011M;
import static com.archive.backend.software.app.ReleaseFixtures.ELITE;
import static com.archive.backend.software.app.ReleaseFixtures.elite;
import static com.archive.backend.software.app.ReleaseFixtures.hardwareSets;
import static com.archive.backend.software.app.ReleaseFixtures.vector;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import com.archive.backend.software.domain.ReleaseId;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CreateReleaseHandlerTest {

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository();
    private final CreateReleaseHandler handler = new CreateReleaseHandler(repository, hardwareSets());

    @Test
    void addsTheReleaseToItsSoftwareUnderANewId() {
        repository.save(elite());

        ReleaseId id = handler.handle(new CreateReleaseCommand(ELITE, AY_MOD_CONTENT, Set.of(AY)));

        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(software -> assertThat(software.release(id)).hasValueSatisfying(release -> {
                    assertThat(release.content()).isEqualTo(AY_MOD_CONTENT);
                    assertThat(release.hardware()).isEqualTo(vector(AY));
                }));
    }

    @Test
    void failsWhenTheSoftwareDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new CreateReleaseCommand(ELITE, AY_MOD_CONTENT, Set.of(AY))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }

    @Test
    void refusesHardwareOfAnotherPlatformAndAddsNothing() {
        repository.save(elite());

        assertThatThrownBy(() -> handler.handle(new CreateReleaseCommand(ELITE, AY_MOD_CONTENT, Set.of(BK0011M))))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("hardware.unknown");
                    assertThat(e.field()).isEqualTo("hardware");
                });
        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(
                        software -> assertThat(software.getReleases()).isEmpty());
    }
}
