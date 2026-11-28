package com.archive.backend.software.app;

import static com.archive.backend.software.app.ReleaseFixtures.ARCADE;
import static com.archive.backend.software.app.ReleaseFixtures.AY;
import static com.archive.backend.software.app.ReleaseFixtures.BK;
import static com.archive.backend.software.app.ReleaseFixtures.BK0011M;
import static com.archive.backend.software.app.ReleaseFixtures.ELITE;
import static com.archive.backend.software.app.ReleaseFixtures.GAMES;
import static com.archive.backend.software.app.ReleaseFixtures.GONE;
import static com.archive.backend.software.app.ReleaseFixtures.VECTOR;
import static com.archive.backend.software.app.ReleaseFixtures.categorySets;
import static com.archive.backend.software.app.ReleaseFixtures.elite;
import static com.archive.backend.software.app.ReleaseFixtures.eliteWithAyMod;
import static com.archive.backend.software.app.ReleaseFixtures.hardwareSets;
import static com.archive.backend.software.app.ReleaseFixtures.in;
import static com.archive.backend.software.app.ReleaseFixtures.vector;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.BY_OTHER;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import com.archive.backend.software.domain.KnownReferences;
import com.archive.backend.software.domain.LineageKind;
import com.archive.backend.software.domain.LineageRework;
import com.archive.backend.software.domain.LineageSource;
import com.archive.backend.software.domain.PreservationStatus;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareCompilationSets;
import com.archive.backend.software.domain.SoftwareCredits;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareInstructions;
import com.archive.backend.software.domain.SoftwareLineage;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.user.domain.ChangeForbiddenException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class UpdateSoftwareHandlerTest {

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository();
    private final UpdateSoftwareHandler handler = new UpdateSoftwareHandler(
            repository,
            hardwareSets(),
            categorySets(),
            KnownReferences.creditSets(reference -> true),
            org.mockito.Mockito.mock(SoftwareCompilationSets.class),
            event -> {});

    @Test
    void storesTheEditedSoftware() {
        repository.save(elite());

        handler.handle(new UpdateSoftwareCommand(
                ADMIN,
                ELITE,
                new SoftwareTitle("Elite Plus"),
                new SoftwareDescription("Enhanced"),
                ReleaseDate.ofDay(LocalDate.of(1987, 1, 1)),
                BK,
                Set.of(),
                Set.of()));

        assertThat(repository.findById(ELITE)).hasValueSatisfying(software -> {
            assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite Plus"));
            assertThat(software.getDescription()).isEqualTo(new SoftwareDescription("Enhanced"));
            assertThat(software.getReleaseDate()).isEqualTo(ReleaseDate.ofDay(LocalDate.of(1987, 1, 1)));
            assertThat(software.getPlatform()).isEqualTo(BK);
        });
    }

    @Test
    void replacesAndClearsTheInstructions() {
        repository.save(elite());

        handler.handle(updateWithInstructions(new SoftwareInstructions("Joystick in port 1.")));
        assertThat(repository.findById(ELITE).map(Software::getInstructions))
                .contains(new SoftwareInstructions("Joystick in port 1."));

        handler.handle(updateWithInstructions(null));
        assertThat(repository.findById(ELITE).map(Software::getInstructions)).isEmpty();
    }

    @Test
    void replacesAndClearsTheLineage() {
        repository.save(elite());
        SoftwareLineage remake = new SoftwareLineage(
                LineageKind.REMAKE,
                null,
                Set.of(LineageRework.NEW_IMPLEMENTATION),
                new LineageSource("Elite, BBC Micro"));

        handler.handle(updateWithLineage(remake));
        assertThat(repository.findById(ELITE).map(Software::getLineage)).contains(remake);

        handler.handle(updateWithLineage(SoftwareLineage.UNKNOWN));
        assertThat(repository.findById(ELITE).map(Software::getLineage)).contains(SoftwareLineage.UNKNOWN);
    }

    private static UpdateSoftwareCommand updateWithLineage(SoftwareLineage lineage) {
        return new UpdateSoftwareCommand(
                ADMIN,
                ELITE,
                new SoftwareTitle("Elite"),
                null,
                null,
                VECTOR,
                Set.of(),
                Set.of(),
                List.of(),
                PreservationStatus.UNKNOWN,
                null,
                null,
                null,
                lineage);
    }

    private static UpdateSoftwareCommand updateWithInstructions(@Nullable SoftwareInstructions instructions) {
        return new UpdateSoftwareCommand(
                ADMIN,
                ELITE,
                new SoftwareTitle("Elite"),
                null,
                null,
                VECTOR,
                Set.of(),
                Set.of(),
                List.of(),
                PreservationStatus.UNKNOWN,
                instructions);
    }

    @Test
    void failsWhenTheSoftwareDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new UpdateSoftwareCommand(
                        ADMIN, ELITE, new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(), Set.of())))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }

    @Test
    void refusesAnUnknownPlatformAndKeepsTheSoftware() {
        repository.save(elite());

        assertThatThrownBy(() -> handler.handle(new UpdateSoftwareCommand(
                        ADMIN,
                        ELITE,
                        new SoftwareTitle("Elite Plus"),
                        null,
                        null,
                        new PlatformCode("zx"),
                        Set.of(),
                        Set.of())))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("software.platform-unknown"));
        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(
                        software -> assertThat(software.getPlatform()).isEqualTo(VECTOR));
    }

    @Test
    void keepsThePlatformWhileAReleaseHasHardware() {
        repository.save(eliteWithAyMod());

        assertThatThrownBy(() -> handler.handle(new UpdateSoftwareCommand(
                        ADMIN, ELITE, new SoftwareTitle("Elite"), null, null, BK, Set.of(), Set.of())))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("software.platform-locked"));
    }

    @Test
    void changesThePlatformTogetherWithHardwareOfTheNewPlatform() {
        repository.save(Software.create(
                ELITE,
                new SoftwareTitle("Elite"),
                null,
                null,
                vector(AY),
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER));

        handler.handle(new UpdateSoftwareCommand(
                ADMIN, ELITE, new SoftwareTitle("Elite"), null, null, BK, Set.of(BK0011M), Set.of()));

        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(software ->
                        assertThat(software.getHardware()).isEqualTo(HardwareSet.restore(BK, Set.of(BK0011M))));
    }

    @Test
    void storesTheHardwareOfTheSoftware() {
        repository.save(elite());

        handler.handle(new UpdateSoftwareCommand(
                ADMIN, ELITE, new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(AY), Set.of()));

        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(
                        software -> assertThat(software.getHardware()).isEqualTo(vector(AY)));
    }

    @Test
    void refusesHardwareOfAnotherPlatformAndKeepsTheHardware() {
        repository.save(Software.create(
                ELITE,
                new SoftwareTitle("Elite"),
                null,
                null,
                vector(AY),
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER));

        assertThatThrownBy(() -> handler.handle(new UpdateSoftwareCommand(
                        ADMIN, ELITE, new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(BK0011M), Set.of())))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("hardware.unknown"));
        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(
                        software -> assertThat(software.getHardware()).isEqualTo(vector(AY)));
    }

    @Test
    void replacesTheCategoriesOfTheSoftware() {
        repository.save(Software.create(
                ELITE, new SoftwareTitle("Elite"), null, null, vector(), in(GAMES), SoftwareCredits.EMPTY, BY_OTHER));

        handler.handle(new UpdateSoftwareCommand(
                ADMIN, ELITE, new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(), Set.of(ARCADE)));

        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(
                        software -> assertThat(software.getCategories()).isEqualTo(in(ARCADE)));
    }

    @Test
    void takesTheSoftwareOutOfACategoryAboveAnotherOfIts() {
        repository.save(Software.create(
                ELITE, new SoftwareTitle("Elite"), null, null, vector(), in(GAMES), SoftwareCredits.EMPTY, BY_OTHER));

        handler.handle(new UpdateSoftwareCommand(
                ADMIN, ELITE, new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(), Set.of(GAMES, ARCADE)));

        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(
                        software -> assertThat(software.getCategories()).isEqualTo(in(ARCADE)));
    }

    @Test
    void takesTheSoftwareOutOfEveryCategory() {
        repository.save(Software.create(
                ELITE, new SoftwareTitle("Elite"), null, null, vector(), in(GAMES), SoftwareCredits.EMPTY, BY_OTHER));

        handler.handle(new UpdateSoftwareCommand(
                ADMIN, ELITE, new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(), Set.of()));

        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(
                        software -> assertThat(software.getCategories()).isEqualTo(CategorySet.EMPTY));
    }

    @Test
    void refusesACategoryNotInTheArchiveAndKeepsTheOldOnes() {
        repository.save(Software.create(
                ELITE, new SoftwareTitle("Elite"), null, null, vector(), in(GAMES), SoftwareCredits.EMPTY, BY_OTHER));

        assertThatThrownBy(() -> handler.handle(new UpdateSoftwareCommand(
                        ADMIN, ELITE, new SoftwareTitle("Elite"), null, null, VECTOR, Set.of(), Set.of(GONE))))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("category.unknown");
                    assertThat(e.field()).isEqualTo("categories");
                });
        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(
                        software -> assertThat(software.getCategories()).isEqualTo(in(GAMES)));
    }

    @Test
    void refusesSomeoneWhoMayNotChangeTheSoftwareAndKeepsIt() {
        repository.save(elite());

        assertThatThrownBy(() -> handler.handle(new UpdateSoftwareCommand(
                        MEMBER, ELITE, new SoftwareTitle("Elite Plus"), null, null, VECTOR, Set.of(), Set.of())))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(software -> assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite")));
    }
}
