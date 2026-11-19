package com.archive.backend.software.domain;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.BY_OTHER;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.PlatformCode;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SoftwareReleaseArchivesTest {

    private static final SoftwareId SOFTWARE = new SoftwareId(id(1));
    private static final ReleaseId RELEASE = new ReleaseId(id(12));
    private static final ReleaseArchive ORIGINAL = new ReleaseArchive(new ReleaseArchiveId(id(21)), "elite.zip", 7);
    private static final ReleaseArchive REPLACEMENT =
            new ReleaseArchive(new ReleaseArchiveId(id(22)), "elite-v2.zip", 8);
    private static final HardwareSet NO_HARDWARE = HardwareSet.restore(new PlatformCode("vector"), Set.of());
    private static final ReleaseContent CONTENT = new ReleaseContent(null, null, ReleaseType.ORIGINAL, null, null);

    private static UUID id(int n) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n));
    }

    private static Software software() {
        Software software = Software.create(
                SOFTWARE,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER);
        software.addRelease(RELEASE, CONTENT, NO_HARDWARE, BY_OTHER, ORIGINAL);
        software.pullEvents();
        return software;
    }

    @Test
    void replacesTheOneArchiveDiscardingTheOldOneAndOutdatingTheFileStructure() {
        Software software = software();

        software.editRelease(RELEASE, CONTENT, NO_HARDWARE, ADMIN, REPLACEMENT, false);

        assertThat(software.release(RELEASE).orElseThrow().archive()).isEqualTo(REPLACEMENT);
        assertThat(software.pullEvents())
                .containsExactly(
                        new ReleaseArchivesDiscarded(List.of(ORIGINAL.id())),
                        new ReleaseFileStructuresOutdated(List.of(RELEASE)));
    }

    @Test
    void removesTheArchiveWithoutRemovingTheReleaseAndOutdatesTheFileStructure() {
        Software software = software();

        software.editRelease(RELEASE, CONTENT, NO_HARDWARE, ADMIN, null, true);

        assertThat(software.release(RELEASE))
                .isPresent()
                .get()
                .extracting(Release::archive)
                .isNull();
        assertThat(software.pullEvents())
                .containsExactly(
                        new ReleaseArchivesDiscarded(List.of(ORIGINAL.id())),
                        new ReleaseFileStructuresOutdated(List.of(RELEASE)));
    }

    @Test
    void removingAReleaseDiscardsItsArchive() {
        Software software = software();

        software.removeRelease(RELEASE, ADMIN);

        assertThat(software.pullEvents())
                .containsExactlyInAnyOrder(
                        new SoftwareChanged(SOFTWARE), new ReleaseArchivesDiscarded(List.of(ORIGINAL.id())));
    }

    @Test
    void deletingSoftwareDiscardsTheArchivesOfItsReleases() {
        Software software = software();

        software.delete();

        assertThat(software.pullEvents())
                .containsExactlyInAnyOrder(
                        new SoftwareChanged(SOFTWARE),
                        new SoftwareDeleted(SOFTWARE),
                        new ReleaseArchivesDiscarded(List.of(ORIGINAL.id())));
    }
}
