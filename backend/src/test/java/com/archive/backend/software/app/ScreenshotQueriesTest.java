package com.archive.backend.software.app;

import static com.archive.backend.user.TestActors.ADDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.software.domain.Gallery;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.read.ReleaseDetails;
import com.archive.backend.software.read.ScreenshotFile;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwarePlatform;
import com.archive.backend.software.read.SoftwareScreenshot;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ScreenshotQueriesTest {

    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID AY_MOD = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final SoftwareScreenshot TITLE =
            new SoftwareScreenshot(UUID.fromString("00000000-0000-0000-0055-000000000001"));
    private static final SoftwareScreenshot GAME =
            new SoftwareScreenshot(UUID.fromString("00000000-0000-0000-0055-000000000002"));
    private static final SoftwareScreenshot AY =
            new SoftwareScreenshot(UUID.fromString("00000000-0000-0000-0055-000000000003"));

    private final InMemorySoftwareReadRepository reads = new InMemorySoftwareReadRepository();
    private final ListScreenshotsHandler list = new ListScreenshotsHandler(reads);

    private void elite() {
        reads.add(new SoftwareDetails(
                ELITE, "Elite", null, null, new SoftwarePlatform("vector", "Vector"), List.of(), List.of(), ADDED));
        reads.addRelease(ELITE, new ReleaseDetails(AY_MOD, null, null, "mod", null, null, List.of()));
        reads.addScreenshots(ELITE, TITLE, GAME);
        reads.addScreenshots(AY_MOD, AY);
    }

    @Test
    void listsTheScreenshotsOfTheSoftwareOrOfItsRelease() {
        elite();

        assertThat(list.handle(new ListScreenshotsQuery(new SoftwareId(ELITE), null)))
                .containsExactly(TITLE, GAME);
        assertThat(list.handle(new ListScreenshotsQuery(new SoftwareId(ELITE), new ReleaseId(AY_MOD))))
                .containsExactly(AY);
    }

    @Test
    void listsThePicturesOfAGalleryOfTheSoftware() {
        elite();
        reads.addPictures(ELITE, "maps", AY);

        assertThat(list.handle(new ListScreenshotsQuery(new SoftwareId(ELITE), null, Gallery.MAPS)))
                .containsExactly(AY);
        assertThat(list.handle(new ListScreenshotsQuery(new SoftwareId(ELITE), null, Gallery.COVERS)))
                .isEmpty();
    }

    @Test
    void failsForUnknownSoftwareOrRelease() {
        assertThatThrownBy(() -> list.handle(new ListScreenshotsQuery(new SoftwareId(ELITE), null)))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
        elite();
        assertThatThrownBy(() -> list.handle(new ListScreenshotsQuery(
                        new SoftwareId(ELITE), new ReleaseId(UUID.fromString("00000000-0000-0000-0000-000000000099")))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("release.not-found"));
    }

    @Test
    void givesTheStoredPictureOfAScreenshot() {
        ScreenshotFile file = new ScreenshotFile(Path.of("/screenshots/1"), "image/png");
        GetScreenshotFileHandler get = new GetScreenshotFileHandler(
                id -> Optional.ofNullable(Map.of(TITLE.id(), file).get(id)));

        assertThat(get.handle(new GetScreenshotFileQuery(new ScreenshotId(TITLE.id()))))
                .isEqualTo(file);
        assertThatThrownBy(() -> get.handle(new GetScreenshotFileQuery(new ScreenshotId(GAME.id()))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("screenshot.not-found"));
    }
}
