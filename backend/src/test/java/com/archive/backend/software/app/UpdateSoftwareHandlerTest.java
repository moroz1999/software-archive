package com.archive.backend.software.app;

import static com.archive.backend.software.app.ReleaseFixtures.AY;
import static com.archive.backend.software.app.ReleaseFixtures.BK;
import static com.archive.backend.software.app.ReleaseFixtures.BK0011M;
import static com.archive.backend.software.app.ReleaseFixtures.ELITE;
import static com.archive.backend.software.app.ReleaseFixtures.VECTOR;
import static com.archive.backend.software.app.ReleaseFixtures.elite;
import static com.archive.backend.software.app.ReleaseFixtures.eliteWithAyMod;
import static com.archive.backend.software.app.ReleaseFixtures.hardwareSets;
import static com.archive.backend.software.app.ReleaseFixtures.vector;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareTitle;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UpdateSoftwareHandlerTest {

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository();
    private final UpdateSoftwareHandler handler = new UpdateSoftwareHandler(repository, hardwareSets());

    @Test
    void storesTheEditedSoftware() {
        repository.save(elite());

        handler.handle(new UpdateSoftwareCommand(
                ELITE,
                new SoftwareTitle("Elite Plus"),
                new SoftwareDescription("Enhanced"),
                LocalDate.of(1987, 1, 1),
                BK,
                Set.of()));

        assertThat(repository.findById(ELITE)).hasValueSatisfying(software -> {
            assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite Plus"));
            assertThat(software.getDescription()).isEqualTo(new SoftwareDescription("Enhanced"));
            assertThat(software.getReleaseDate()).isEqualTo(LocalDate.of(1987, 1, 1));
            assertThat(software.getPlatform()).isEqualTo(BK);
        });
    }

    @Test
    void failsWhenTheSoftwareDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(
                        new UpdateSoftwareCommand(ELITE, new SoftwareTitle("Elite"), null, null, VECTOR, Set.of())))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }

    @Test
    void refusesAnUnknownPlatformAndKeepsTheSoftware() {
        repository.save(elite());

        assertThatThrownBy(() -> handler.handle(new UpdateSoftwareCommand(
                        ELITE, new SoftwareTitle("Elite Plus"), null, null, new PlatformCode("zx"), Set.of())))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("software.platform-unknown"));
        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(
                        software -> assertThat(software.getPlatform()).isEqualTo(VECTOR));
    }

    @Test
    void keepsThePlatformWhileAReleaseHasHardware() {
        repository.save(eliteWithAyMod());

        assertThatThrownBy(() -> handler.handle(
                        new UpdateSoftwareCommand(ELITE, new SoftwareTitle("Elite"), null, null, BK, Set.of())))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("software.platform-locked"));
    }

    @Test
    void changesThePlatformTogetherWithHardwareOfTheNewPlatform() {
        repository.save(Software.create(ELITE, new SoftwareTitle("Elite"), null, null, vector(AY)));

        handler.handle(new UpdateSoftwareCommand(ELITE, new SoftwareTitle("Elite"), null, null, BK, Set.of(BK0011M)));

        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(software ->
                        assertThat(software.getHardware()).isEqualTo(HardwareSet.restore(BK, Set.of(BK0011M))));
    }

    @Test
    void storesTheHardwareOfTheSoftware() {
        repository.save(elite());

        handler.handle(new UpdateSoftwareCommand(ELITE, new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(AY)));

        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(
                        software -> assertThat(software.getHardware()).isEqualTo(vector(AY)));
    }

    @Test
    void refusesHardwareOfAnotherPlatformAndKeepsTheHardware() {
        repository.save(Software.create(ELITE, new SoftwareTitle("Elite"), null, null, vector(AY)));

        assertThatThrownBy(() -> handler.handle(new UpdateSoftwareCommand(
                        ELITE, new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(BK0011M))))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("hardware.unknown"));
        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(
                        software -> assertThat(software.getHardware()).isEqualTo(vector(AY)));
    }
}
