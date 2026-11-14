package com.archive.backend.software.domain;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static com.archive.backend.user.TestActors.OTHER;
import static com.archive.backend.user.TestActors.OTHER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.AddedBy;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Who may change software and its releases (docs/domain/users.md). */
class SoftwarePermissionsTest {

    private static final SoftwareId ID = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final ReleaseId RELEASE = new ReleaseId(UUID.fromString("00000000-0000-0000-0000-000000000011"));
    private static final HardwareSet NO_HARDWARE = HardwareSet.restore(new PlatformCode("vector"), Set.of());
    private static final ReleaseContent CONTENT = new ReleaseContent(null, null, ReleaseType.ORIGINAL, null, null);

    private static Software addedBy(AddedBy addedBy) {
        return Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                NO_HARDWARE,
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                addedBy);
    }

    /** Software of the member with a release another member added. */
    private static Software withReleaseOfOther() {
        Software software = addedBy(new AddedBy(MEMBER_ID));
        software.addRelease(RELEASE, CONTENT, NO_HARDWARE, new AddedBy(OTHER_ID));
        return software;
    }

    @Test
    void remembersWhoAddedIt() {
        assertThat(addedBy(new AddedBy(MEMBER_ID)).getAddedBy()).isEqualTo(new AddedBy(MEMBER_ID));
    }

    @Test
    void isChangedByWhoeverAddedItAndByAdministrators() {
        Software software = addedBy(new AddedBy(MEMBER_ID));

        assertThatCode(() -> software.requireChangeableBy(MEMBER)).doesNotThrowAnyException();
        assertThatCode(() -> software.requireChangeableBy(ADMIN)).doesNotThrowAnyException();
        assertThatThrownBy(() -> software.requireChangeableBy(OTHER)).isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> software.requireChangeableBy(Actor.anonymous()))
                .isInstanceOf(SignInRequiredException.class);
    }

    @Test
    void ofNobodyIsChangedByAdministratorsOnly() {
        Software software = addedBy(AddedBy.NOBODY);

        assertThatCode(() -> software.requireChangeableBy(ADMIN)).doesNotThrowAnyException();
        assertThatThrownBy(() -> software.requireChangeableBy(MEMBER)).isInstanceOf(ChangeForbiddenException.class);
    }

    @Test
    void remembersWhoAddedARelease() {
        assertThat(withReleaseOfOther().release(RELEASE).orElseThrow().addedBy())
                .isEqualTo(new AddedBy(OTHER_ID));
    }

    @Test
    void aReleaseIsChangedByWhoeverAddedIt() {
        Software software = withReleaseOfOther();

        software.editRelease(RELEASE, CONTENT, NO_HARDWARE, OTHER);
        software.removeRelease(RELEASE, OTHER);

        assertThat(software.getReleases()).isEmpty();
    }

    @Test
    void aReleaseIsChangedByWhoeverAddedItsSoftware() {
        Software software = withReleaseOfOther();

        software.editRelease(RELEASE, CONTENT, NO_HARDWARE, MEMBER);
        software.removeRelease(RELEASE, MEMBER);

        assertThat(software.getReleases()).isEmpty();
    }

    @Test
    void aReleaseIsChangedByAdministrators() {
        Software software = withReleaseOfOther();

        software.removeRelease(RELEASE, ADMIN);

        assertThat(software.getReleases()).isEmpty();
    }

    @Test
    void aReleaseIsNotChangedBySomeoneElse() {
        Software software = addedBy(new AddedBy(MEMBER_ID));
        software.addRelease(RELEASE, CONTENT, NO_HARDWARE, new AddedBy(MEMBER_ID));

        assertThatThrownBy(() -> software.editRelease(RELEASE, CONTENT, NO_HARDWARE, OTHER))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> software.removeRelease(RELEASE, OTHER)).isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> software.removeRelease(RELEASE, Actor.anonymous()))
                .isInstanceOf(SignInRequiredException.class);
        assertThat(software.getReleases()).hasSize(1);
    }
}
