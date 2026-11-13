package com.archive.backend.software.app;

import static com.archive.backend.software.app.ReleaseFixtures.AY_MOD;
import static com.archive.backend.software.app.ReleaseFixtures.ELITE;
import static com.archive.backend.software.app.ReleaseFixtures.UNKNOWN;
import static com.archive.backend.software.app.ReleaseFixtures.eliteWithAyMod;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.software.app.ScreenshotFixtures.RecordingEvents;
import com.archive.backend.software.app.ScreenshotFixtures.RecordingScreenshotFiles;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.Screenshot;
import com.archive.backend.software.domain.ScreenshotFormat;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.ScreenshotImages;
import com.archive.backend.software.domain.ScreenshotOrderOutdatedException;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareChanged;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

/** Adding, deleting and reordering the screenshots of software and of its releases. */
class ScreenshotHandlersTest {

    private static final ScreenshotImage PNG = ScreenshotImages.image(ScreenshotImages.png());
    private static final ScreenshotImage GIF = ScreenshotImages.image(ScreenshotImages.gif());

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository();
    private final RecordingScreenshotFiles files = new RecordingScreenshotFiles();
    private final RecordingEvents events = new RecordingEvents(files);
    private final AddScreenshotsHandler add = new AddScreenshotsHandler(repository, files, events);
    private final DeleteScreenshotHandler delete = new DeleteScreenshotHandler(repository, events);
    private final ReorderScreenshotsHandler reorder = new ReorderScreenshotsHandler(repository, events);

    private Software stored() {
        return repository.findById(ELITE).orElseThrow();
    }

    private List<ScreenshotId> added(@Nullable ReleaseId release, ScreenshotImage... images) {
        List<ScreenshotId> ids = add.handle(new AddScreenshotsCommand(ADMIN, ELITE, release, List.of(images)));
        events.published.clear();
        return ids;
    }

    @Test
    void addsScreenshotsToTheSoftwareAndStoresTheirPictures() {
        repository.save(eliteWithAyMod());

        List<ScreenshotId> ids = add.handle(new AddScreenshotsCommand(ADMIN, ELITE, null, List.of(PNG, GIF)));

        assertThat(ids).hasSize(2).doesNotHaveDuplicates();
        assertThat(stored().screenshots(null).all())
                .containsExactly(
                        new Screenshot(ids.get(0), ScreenshotFormat.PNG),
                        new Screenshot(ids.get(1), ScreenshotFormat.GIF));
        assertThat(files.stored).containsOnlyKeys(ids);
        assertThat(files.stored.get(ids.get(1))).isSameAs(GIF);
        assertThat(events.published).containsExactly(new SoftwareChanged(ELITE));
    }

    @Test
    void addsScreenshotsToARelease() {
        repository.save(eliteWithAyMod());

        List<ScreenshotId> ids = add.handle(new AddScreenshotsCommand(ADMIN, ELITE, AY_MOD, List.of(PNG)));

        assertThat(stored().screenshots(AY_MOD).ids()).isEqualTo(ids);
        assertThat(stored().screenshots(null).all()).isEmpty();
        assertThat(files.stored).containsOnlyKeys(ids);
    }

    @Test
    void storesNoPictureForSomeoneWhoMayNotAddThem() {
        repository.save(eliteWithAyMod());

        assertThatThrownBy(() -> add.handle(new AddScreenshotsCommand(MEMBER, ELITE, null, List.of(PNG))))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> add.handle(new AddScreenshotsCommand(Actor.anonymous(), ELITE, AY_MOD, List.of(PNG))))
                .isInstanceOf(SignInRequiredException.class);
        assertThat(files.stored).isEmpty();
        assertThat(stored().screenshots(null).all()).isEmpty();
    }

    @Test
    void failsForUnknownSoftwareOrReleaseStoringNothing() {
        assertThatThrownBy(() -> add.handle(new AddScreenshotsCommand(ADMIN, ELITE, null, List.of(PNG))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
        repository.save(eliteWithAyMod());
        assertThatThrownBy(() -> add.handle(new AddScreenshotsCommand(ADMIN, ELITE, UNKNOWN, List.of(PNG))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("release.not-found"));
        assertThat(files.stored).isEmpty();
    }

    @Test
    void deletesAScreenshotAndDiscardsItsPicture() {
        repository.save(eliteWithAyMod());
        List<ScreenshotId> ids = added(null, PNG, GIF);

        delete.handle(new DeleteScreenshotCommand(ADMIN, ELITE, null, ids.get(0)));

        assertThat(stored().screenshots(null).ids()).containsExactly(ids.get(1));
        assertThat(files.discarded).containsExactly(ids.get(0));
        assertThat(events.published).contains(new SoftwareChanged(ELITE));
    }

    @Test
    void failsToDeleteAnUnknownScreenshot() {
        repository.save(eliteWithAyMod());
        List<ScreenshotId> ids = added(AY_MOD, PNG);

        assertThatThrownBy(() -> delete.handle(new DeleteScreenshotCommand(ADMIN, ELITE, null, ids.get(0))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("screenshot.not-found"));
        assertThat(files.discarded).isEmpty();
    }

    @Test
    void refusesToDeleteForSomeoneWhoMayNotAndKeepsThePicture() {
        repository.save(eliteWithAyMod());
        List<ScreenshotId> ids = added(AY_MOD, PNG);

        assertThatThrownBy(() -> delete.handle(new DeleteScreenshotCommand(MEMBER, ELITE, AY_MOD, ids.get(0))))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(stored().screenshots(AY_MOD).ids()).isEqualTo(ids);
        assertThat(files.discarded).isEmpty();
    }

    @Test
    void reordersTheScreenshots() {
        repository.save(eliteWithAyMod());
        List<ScreenshotId> ids = added(AY_MOD, PNG, GIF);

        reorder.handle(new ReorderScreenshotsCommand(ADMIN, ELITE, AY_MOD, List.of(ids.get(1), ids.get(0))));

        assertThat(stored().screenshots(AY_MOD).ids()).containsExactly(ids.get(1), ids.get(0));
        assertThat(events.published).containsExactly(new SoftwareChanged(ELITE));
    }

    @Test
    void refusesAnOutdatedOrderAndSomeoneWhoMayNotReorder() {
        repository.save(eliteWithAyMod());
        List<ScreenshotId> ids = added(null, PNG, GIF);

        assertThatThrownBy(() -> reorder.handle(new ReorderScreenshotsCommand(ADMIN, ELITE, null, List.of(ids.get(1)))))
                .isInstanceOf(ScreenshotOrderOutdatedException.class);
        assertThatThrownBy(() -> reorder.handle(
                        new ReorderScreenshotsCommand(MEMBER, ELITE, null, List.of(ids.get(1), ids.get(0)))))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(stored().screenshots(null).ids()).isEqualTo(ids);
    }

    @Test
    void deletingAReleaseDiscardsThePicturesOfItsScreenshots() {
        repository.save(eliteWithAyMod());
        List<ScreenshotId> ids = added(AY_MOD, PNG, GIF);

        new DeleteReleaseHandler(repository, events).handle(new DeleteReleaseCommand(ADMIN, ELITE, AY_MOD));

        assertThat(files.discarded).containsExactlyElementsOf(ids);
        assertThat(events.published).contains(new SoftwareChanged(ELITE));
    }

    @Test
    void deletingTheSoftwareDiscardsThePicturesOfAllItsScreenshots() {
        repository.save(eliteWithAyMod());
        List<ScreenshotId> own = added(null, PNG);
        List<ScreenshotId> ofRelease = added(AY_MOD, GIF);

        new DeleteSoftwareHandler(repository, events).handle(new DeleteSoftwareCommand(ADMIN, ELITE));

        assertThat(files.discarded).containsExactlyInAnyOrder(own.get(0), ofRelease.get(0));
    }
}
