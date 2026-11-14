package com.archive.backend.software.domain;

import static com.archive.backend.software.domain.ScreenshotsTest.shot;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static com.archive.backend.user.TestActors.OTHER;
import static com.archive.backend.user.TestActors.OTHER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.AddedBy;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** The screenshots of software and of its releases (docs/domain/software.md#screenshots). */
class SoftwareScreenshotsTest {

    private static final SoftwareId ID = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final ReleaseId RELEASE = new ReleaseId(UUID.fromString("00000000-0000-0000-0000-000000000011"));
    private static final ReleaseId UNKNOWN = new ReleaseId(UUID.fromString("00000000-0000-0000-0000-000000000099"));
    private static final HardwareSet NO_HARDWARE = HardwareSet.restore(new PlatformCode("vector"), Set.of());
    private static final ReleaseContent CONTENT = new ReleaseContent(null, null, ReleaseType.ORIGINAL, null, null);

    /** Software of the member with a release another member added, and nothing changed since it was loaded. */
    private static Software software() {
        Software software = Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareReferences.EMPTY,
                SoftwareCredits.EMPTY,
                new AddedBy(MEMBER_ID));
        software.addRelease(RELEASE, CONTENT, NO_HARDWARE, new AddedBy(OTHER_ID));
        software.pullEvents();
        return software;
    }

    private static Software withShots() {
        Software software = software();
        software.addScreenshots(null, List.of(shot(1), shot(2)), MEMBER);
        software.addScreenshots(RELEASE, List.of(shot(3), shot(4)), OTHER);
        software.pullEvents();
        return software;
    }

    @Test
    void addsScreenshotsToTheSoftwareAfterThoseThereAre() {
        Software software = software();

        software.addScreenshots(null, List.of(shot(1)), MEMBER);
        software.addScreenshots(null, List.of(shot(2), shot(3)), MEMBER);

        assertThat(software.screenshots(null).all()).containsExactly(shot(1), shot(2), shot(3));
        assertThat(software.screenshots(RELEASE).all()).isEmpty();
        assertThat(software.pullEvents()).containsExactly(new SoftwareChanged(ID));
    }

    @Test
    void addsScreenshotsToARelease() {
        Software software = software();

        software.addScreenshots(RELEASE, List.of(shot(1)), OTHER);

        assertThat(software.screenshots(RELEASE).all()).containsExactly(shot(1));
        assertThat(software.screenshots(null).all()).isEmpty();
        // The card of software without its own screenshots shows those of its releases.
        assertThat(software.pullEvents()).containsExactly(new SoftwareChanged(ID));
    }

    @Test
    void failsForAReleaseItDoesNotHave() {
        Software software = software();

        assertThatThrownBy(() -> software.addScreenshots(UNKNOWN, List.of(shot(1)), ADMIN))
                .isInstanceOf(ReleaseNotFoundException.class);
        assertThatThrownBy(() -> software.screenshots(UNKNOWN)).isInstanceOf(ReleaseNotFoundException.class);
    }

    @Test
    void removesAScreenshotAndTellsItsPictureIsDiscarded() {
        Software software = withShots();

        software.removeScreenshot(null, shot(1).id(), MEMBER);
        software.removeScreenshot(RELEASE, shot(4).id(), OTHER);

        assertThat(software.screenshots(null).all()).containsExactly(shot(2));
        assertThat(software.screenshots(RELEASE).all()).containsExactly(shot(3));
        assertThat(software.pullEvents())
                .containsExactlyInAnyOrder(
                        new SoftwareChanged(ID),
                        new ScreenshotsDiscarded(List.of(shot(1).id())),
                        new ScreenshotsDiscarded(List.of(shot(4).id())));
    }

    @Test
    void doesNotRemoveAScreenshotOfAnotherPlace() {
        Software software = withShots();

        assertThatThrownBy(() -> software.removeScreenshot(null, shot(3).id(), ADMIN))
                .isInstanceOf(ScreenshotNotFoundException.class);
        assertThatThrownBy(() -> software.removeScreenshot(RELEASE, shot(1).id(), ADMIN))
                .isInstanceOf(ScreenshotNotFoundException.class);
    }

    @Test
    void reordersTheScreenshots() {
        Software software = withShots();

        software.reorderScreenshots(null, List.of(shot(2).id(), shot(1).id()), MEMBER);
        software.reorderScreenshots(RELEASE, List.of(shot(4).id(), shot(3).id()), OTHER);

        assertThat(software.screenshots(null).all()).containsExactly(shot(2), shot(1));
        assertThat(software.screenshots(RELEASE).all()).containsExactly(shot(4), shot(3));
        assertThat(software.pullEvents()).containsExactly(new SoftwareChanged(ID));
    }

    @Test
    void refusesAnOutdatedOrder() {
        Software software = withShots();

        assertThatThrownBy(() -> software.reorderScreenshots(null, List.of(shot(2).id()), MEMBER))
                .isInstanceOf(ScreenshotOrderOutdatedException.class);
        assertThat(software.screenshots(null).all()).containsExactly(shot(1), shot(2));
    }

    @Test
    void screenshotsOfTheSoftwareAreChangedByWhoeverMayChangeTheSoftware() {
        Software software = withShots();

        assertThatThrownBy(() -> software.addScreenshots(null, List.of(shot(5)), OTHER))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> software.removeScreenshot(null, shot(1).id(), OTHER))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> software.reorderScreenshots(null, List.of(shot(2).id(), shot(1).id()), OTHER))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> software.addScreenshots(null, List.of(shot(5)), Actor.anonymous()))
                .isInstanceOf(SignInRequiredException.class);

        software.addScreenshots(null, List.of(shot(5)), ADMIN);
        assertThat(software.screenshots(null).all()).containsExactly(shot(1), shot(2), shot(5));
    }

    @Test
    void screenshotsOfAReleaseAreChangedByWhoeverMayChangeTheRelease() {
        Software software = withShots();
        Actor stranger = new Actor(
                new UserId(UUID.fromString("00000000-0000-0000-00aa-000000000009")), Set.of(UserGroup.MEMBER));

        assertThatThrownBy(() -> software.addScreenshots(RELEASE, List.of(shot(5)), stranger))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> software.removeScreenshot(RELEASE, shot(3).id(), stranger))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> software.reorderScreenshots(RELEASE, List.of(shot(4).id(), shot(3).id()), stranger))
                .isInstanceOf(ChangeForbiddenException.class);

        // Whoever added the software changes its releases too.
        software.addScreenshots(RELEASE, List.of(shot(5)), MEMBER);
        assertThat(software.screenshots(RELEASE).all()).containsExactly(shot(3), shot(4), shot(5));
    }

    @Test
    void removingAReleaseDiscardsItsScreenshots() {
        Software software = withShots();

        software.removeRelease(RELEASE, OTHER);

        assertThat(software.pullEvents())
                .containsExactlyInAnyOrder(
                        new SoftwareChanged(ID), new ScreenshotsDiscarded(List.of(shot(3).id(), shot(4).id())));
    }

    @Test
    void deletingTheSoftwareDiscardsEveryScreenshot() {
        Software software = withShots();

        software.delete();

        assertThat(software.pullEvents())
                .containsExactlyInAnyOrder(
                        new SoftwareChanged(ID),
                        new SoftwareDeleted(ID),
                        new ScreenshotsDiscarded(List.of(shot(1).id(), shot(2).id(), shot(3).id(), shot(4).id())));
    }
}
