package com.archive.backend.software.domain;

import static com.archive.backend.software.domain.ScreenshotsTest.shot;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static com.archive.backend.user.TestActors.OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.user.domain.AddedBy;
import com.archive.backend.user.domain.ChangeForbiddenException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** The maps, covers and adverts of software, each a gallery of its own beside the screenshots (docs/domain/software.md#pictures). */
class SoftwareGalleriesTest {

    private static final SoftwareId ID = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final HardwareSet NO_HARDWARE = HardwareSet.restore(new PlatformCode("vector"), Set.of());

    /** Software of the member with a screenshot, a map and a cover, and nothing changed since it was loaded. */
    private static Software software() {
        Software software = Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                new AddedBy(MEMBER_ID));
        software.addScreenshots(null, List.of(shot(1)), MEMBER);
        software.addPictures(Gallery.MAPS, List.of(shot(2), shot(3)), MEMBER);
        software.addPictures(Gallery.COVERS, List.of(shot(4)), MEMBER);
        software.pullEvents();
        return software;
    }

    @Test
    void keepsEveryGalleryApart() {
        Software software = software();

        software.addPictures(Gallery.ADVERTS, List.of(shot(5)), MEMBER);

        assertThat(software.pictures(Gallery.SCREENSHOTS).all()).containsExactly(shot(1));
        assertThat(software.pictures(Gallery.MAPS).all()).containsExactly(shot(2), shot(3));
        assertThat(software.pictures(Gallery.COVERS).all()).containsExactly(shot(4));
        assertThat(software.pictures(Gallery.ADVERTS).all()).containsExactly(shot(5));
        assertThat(software.screenshots(null).all()).containsExactly(shot(1));
    }

    @Test
    void removesAndReordersAPictureOnlyInItsGallery() {
        Software software = software();

        software.reorderPictures(Gallery.MAPS, List.of(shot(3).id(), shot(2).id()), MEMBER);
        software.removePicture(Gallery.COVERS, shot(4).id(), MEMBER);

        assertThat(software.pictures(Gallery.MAPS).all()).containsExactly(shot(3), shot(2));
        assertThat(software.pictures(Gallery.COVERS).all()).isEmpty();
        assertThat(software.pullEvents()).contains(new ScreenshotsDiscarded(List.of(shot(4).id())));
        assertThatThrownBy(() -> software.removePicture(Gallery.MAPS, shot(1).id(), MEMBER))
                .isInstanceOf(ScreenshotNotFoundException.class);
    }

    @Test
    void picturesAreChangedByWhoeverMayChangeTheSoftware() {
        Software software = software();

        assertThatThrownBy(() -> software.addPictures(Gallery.ADVERTS, List.of(shot(5)), OTHER))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> software.removePicture(Gallery.MAPS, shot(2).id(), OTHER))
                .isInstanceOf(ChangeForbiddenException.class);

        software.addPictures(Gallery.ADVERTS, List.of(shot(5)), ADMIN);
        assertThat(software.pictures(Gallery.ADVERTS).all()).containsExactly(shot(5));
    }

    @Test
    void deletingTheSoftwareDiscardsThePicturesOfEveryGallery() {
        Software software = software();

        software.delete();

        assertThat(software.pullEvents())
                .contains(new ScreenshotsDiscarded(List.of(shot(1).id(), shot(2).id(), shot(3).id(), shot(4).id())));
    }

    @Test
    void keepsItsInstructionsApartFromItsDescription() {
        Software software = software();

        software.changeInstructions(new SoftwareInstructions("LOAD \"\" and press any key."));

        assertThat(software.getInstructions()).isEqualTo(new SoftwareInstructions("LOAD \"\" and press any key."));
        assertThat(software.getDescription()).isNull();
    }
}
