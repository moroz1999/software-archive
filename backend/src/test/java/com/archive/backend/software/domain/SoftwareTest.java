package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SoftwareTest {

    private static final SoftwareId ID = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlatformCode VECTOR = new PlatformCode("vector");
    private static final PlatformCode BK = new PlatformCode("bk");
    private static final ReleaseId ORIGINAL = new ReleaseId(UUID.fromString("00000000-0000-0000-0000-000000000011"));
    private static final ReleaseId AY_MOD = new ReleaseId(UUID.fromString("00000000-0000-0000-0000-000000000012"));
    private static final HardwareCode AY = new HardwareCode("ay8910");
    private static final HardwareCode VECTOR_06C = new HardwareCode("vector06c");
    private static final HardwareCode BK0011M = new HardwareCode("bk0011m");

    // Sets the reference book has already checked: the rules under test are those of the software itself.
    private static final HardwareSet NO_VECTOR_HARDWARE = HardwareSet.restore(VECTOR, Set.of());
    private static final HardwareSet NO_BK_HARDWARE = HardwareSet.restore(BK, Set.of());
    private static final HardwareSet VECTOR_AY = HardwareSet.restore(VECTOR, Set.of(AY));
    private static final HardwareSet VECTOR_06C_SET = HardwareSet.restore(VECTOR, Set.of(VECTOR_06C));
    private static final HardwareSet BK_0011M = HardwareSet.restore(BK, Set.of(BK0011M));

    private static final ReleaseContent ORIGINAL_CONTENT = content(ReleaseType.ORIGINAL);
    private static final ReleaseContent AY_MOD_CONTENT = new ReleaseContent(
            new ReleaseTitle("Elite AY"),
            new ReleaseVersion("1.1"),
            ReleaseType.MOD,
            LocalDate.of(1990, 3, 1),
            new ReleaseDescription("With AY music"));

    private static ReleaseContent content(ReleaseType type) {
        return new ReleaseContent(null, null, type, null, null);
    }

    private static Software elite() {
        return Software.create(ID, new SoftwareTitle("Elite"), null, null, NO_VECTOR_HARDWARE);
    }

    private static Software eliteWithAyMod() {
        Software software = elite();
        software.addRelease(AY_MOD, AY_MOD_CONTENT, VECTOR_AY);
        return software;
    }

    @Test
    void isCreatedWithItsTitleDescriptionReleaseDateAndPlatform() {
        Software software = Software.create(
                ID,
                new SoftwareTitle("Elite"),
                new SoftwareDescription("Space trading"),
                LocalDate.of(1985, 9, 20),
                NO_VECTOR_HARDWARE);

        assertThat(software.getId()).isEqualTo(ID);
        assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite"));
        assertThat(software.getDescription()).isEqualTo(new SoftwareDescription("Space trading"));
        assertThat(software.getReleaseDate()).isEqualTo(LocalDate.of(1985, 9, 20));
        assertThat(software.getPlatform()).isEqualTo(VECTOR);
    }

    @Test
    void takesItsPlatformFromItsHardware() {
        Software software = Software.create(ID, new SoftwareTitle("Elite"), null, null, VECTOR_06C_SET);

        assertThat(software.getPlatform()).isEqualTo(VECTOR);
        assertThat(software.getHardware()).isEqualTo(VECTOR_06C_SET);
    }

    @Test
    void isCreatedWithoutReleases() {
        assertThat(elite().getReleases()).isEmpty();
    }

    @Test
    void editingReplacesTitleDescriptionReleaseDateAndHardware() {
        Software software = Software.create(
                ID,
                new SoftwareTitle("Elite"),
                new SoftwareDescription("Space trading"),
                LocalDate.of(1985, 9, 20),
                VECTOR_06C_SET);

        software.edit(new SoftwareTitle("Elite Plus"), null, null, VECTOR_AY);

        assertThat(software.getId()).isEqualTo(ID);
        assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite Plus"));
        assertThat(software.getDescription()).isNull();
        assertThat(software.getReleaseDate()).isNull();
        assertThat(software.getHardware()).isEqualTo(VECTOR_AY);
    }

    @Test
    void addsReleasesWithWhatTheyStateAndTheirHardware() {
        Software software = elite();

        software.addRelease(ORIGINAL, ORIGINAL_CONTENT, NO_VECTOR_HARDWARE);
        software.addRelease(AY_MOD, AY_MOD_CONTENT, VECTOR_AY);

        assertThat(software.getReleases()).extracting(Release::id).containsExactly(ORIGINAL, AY_MOD);
        assertThat(software.release(AY_MOD)).hasValueSatisfying(release -> {
            assertThat(release.content()).isEqualTo(AY_MOD_CONTENT);
            assertThat(release.hardware()).isEqualTo(VECTOR_AY);
        });
    }

    @Test
    void editsARelease() {
        Software software = eliteWithAyMod();

        software.editRelease(AY_MOD, content(ReleaseType.BUGFIX), NO_VECTOR_HARDWARE);

        assertThat(software.release(AY_MOD)).hasValueSatisfying(release -> {
            assertThat(release.content()).isEqualTo(content(ReleaseType.BUGFIX));
            assertThat(release.hardware()).isEqualTo(NO_VECTOR_HARDWARE);
        });
    }

    @Test
    void removesARelease() {
        Software software = eliteWithAyMod();
        software.addRelease(ORIGINAL, ORIGINAL_CONTENT, NO_VECTOR_HARDWARE);

        software.removeRelease(ORIGINAL);

        assertThat(software.getReleases()).extracting(Release::id).containsExactly(AY_MOD);
        assertThat(software.release(ORIGINAL)).isEmpty();
    }

    @Test
    void doesNotKnowAnyOtherRelease() {
        Software software = elite();
        software.addRelease(ORIGINAL, ORIGINAL_CONTENT, NO_VECTOR_HARDWARE);

        assertThatThrownBy(() -> software.editRelease(AY_MOD, AY_MOD_CONTENT, VECTOR_AY))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("release.not-found"));
        assertThatThrownBy(() -> software.removeRelease(AY_MOD))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("release.not-found"));
        assertThat(software.getReleases()).extracting(Release::id).containsExactly(ORIGINAL);
    }

    @Test
    void refusesAReleaseWithHardwareOfAnotherPlatform() {
        Software software = elite();

        assertThatThrownBy(() -> software.addRelease(AY_MOD, AY_MOD_CONTENT, BK_0011M))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("hardware.unknown");
                    assertThat(e.field()).isEqualTo("hardware");
                });
        assertThat(software.getReleases()).isEmpty();
    }

    @Test
    void refusesToEditAReleaseToHardwareOfAnotherPlatform() {
        Software software = eliteWithAyMod();

        assertThatThrownBy(() -> software.editRelease(AY_MOD, content(ReleaseType.BUGFIX), BK_0011M))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("hardware.unknown"));
        assertThat(software.release(AY_MOD)).hasValueSatisfying(release -> {
            assertThat(release.content()).isEqualTo(AY_MOD_CONTENT);
            assertThat(release.hardware()).isEqualTo(VECTOR_AY);
        });
    }

    @Test
    void takesAReleaseWithoutHardwareWhateverPlatformItsEmptySetNames() {
        Software software = elite();

        software.addRelease(ORIGINAL, ORIGINAL_CONTENT, NO_BK_HARDWARE);

        assertThat(software.getReleases()).extracting(Release::id).containsExactly(ORIGINAL);
    }

    @Test
    void cannotExistWithAReleaseOfHardwareOfAnotherPlatform() {
        assertThatThrownBy(() -> new Software(
                        ID,
                        new SoftwareTitle("Elite"),
                        null,
                        null,
                        NO_VECTOR_HARDWARE,
                        List.of(new Release(AY_MOD, AY_MOD_CONTENT, BK_0011M))))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("hardware.unknown"));
    }

    @Test
    void changesThePlatformWhileNoReleaseHasHardware() {
        Software software = elite();
        software.addRelease(ORIGINAL, ORIGINAL_CONTENT, NO_VECTOR_HARDWARE);

        software.edit(new SoftwareTitle("Elite"), null, null, NO_BK_HARDWARE);

        assertThat(software.getPlatform()).isEqualTo(BK);
    }

    @Test
    void changesThePlatformTogetherWithHardwareOfTheNewPlatform() {
        Software software = Software.create(ID, new SoftwareTitle("Elite"), null, null, VECTOR_06C_SET);

        software.edit(new SoftwareTitle("Elite"), null, null, BK_0011M);

        assertThat(software.getPlatform()).isEqualTo(BK);
        assertThat(software.getHardware()).isEqualTo(BK_0011M);
    }

    @Test
    void keepsThePlatformWhileAReleaseHasHardware() {
        Software software = eliteWithAyMod();

        assertThatThrownBy(() -> software.edit(new SoftwareTitle("Elite Plus"), null, null, NO_BK_HARDWARE))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("software.platform-locked");
                    assertThat(e.field()).isEqualTo("platform");
                });
        assertThat(software.getPlatform()).isEqualTo(VECTOR);
        assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite"));
    }

    @Test
    void editsTheRestOfSoftwareWhoseReleasesHaveHardwareOnTheSamePlatform() {
        Software software = eliteWithAyMod();

        software.edit(new SoftwareTitle("Elite Plus"), null, null, VECTOR_06C_SET);

        assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite Plus"));
        assertThat(software.getHardware()).isEqualTo(VECTOR_06C_SET);
    }
}
