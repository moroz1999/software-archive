package com.archive.backend.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PermissionsTest {

    private static final UserId ZED = new UserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final UserId AMY = new UserId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    private static final Actor MEMBER_ZED = new Actor(ZED, Set.of(UserGroup.MEMBER));
    private static final Actor MEMBER_AMY = new Actor(AMY, Set.of(UserGroup.MEMBER));
    private static final Actor ADMIN_AMY = new Actor(AMY, Set.of(UserGroup.MEMBER, UserGroup.ADMIN));

    @Test
    void aSignedInUserAddsEntries() {
        assertThat(AddedBy.of(MEMBER_ZED)).isEqualTo(new AddedBy(ZED));
    }

    @Test
    void aPublicVisitorIsToldToSignInToAdd() {
        assertThatThrownBy(() -> AddedBy.of(Actor.anonymous())).isInstanceOf(SignInRequiredException.class);
    }

    @Test
    void whoeverAddedAnEntryChangesIt() {
        assertThat(new AddedBy(ZED).isBy(MEMBER_ZED)).isTrue();
        assertThat(new AddedBy(ZED).isBy(MEMBER_AMY)).isFalse();
        assertThat(AddedBy.NOBODY.isBy(Actor.anonymous())).isFalse();
        assertThatCode(() -> new AddedBy(ZED).requireChangeableBy(MEMBER_ZED)).doesNotThrowAnyException();
    }

    @Test
    void someoneElseMayNotChangeIt() {
        assertThatThrownBy(() -> new AddedBy(ZED).requireChangeableBy(MEMBER_AMY))
                .isInstanceOf(ChangeForbiddenException.class)
                .hasFieldOrPropertyWithValue("code", "access.denied");
    }

    @Test
    void aPublicVisitorIsToldToSignInToChangeIt() {
        assertThatThrownBy(() -> new AddedBy(ZED).requireChangeableBy(Actor.anonymous()))
                .isInstanceOf(SignInRequiredException.class);
    }

    @Test
    void anAdministratorChangesAnyEntry() {
        assertThat(ADMIN_AMY.isAdministrator()).isTrue();
        assertThat(MEMBER_AMY.isAdministrator()).isFalse();
        assertThatCode(() -> new AddedBy(ZED).requireChangeableBy(ADMIN_AMY)).doesNotThrowAnyException();
        assertThatCode(() -> AddedBy.NOBODY.requireChangeableBy(ADMIN_AMY)).doesNotThrowAnyException();
    }

    @Test
    void anEntryOfNobodyIsForAdministratorsOnly() {
        assertThatThrownBy(() -> AddedBy.NOBODY.requireChangeableBy(MEMBER_ZED))
                .isInstanceOf(ChangeForbiddenException.class);
    }

    @Test
    void anActionNotAllowedTellsWhy() {
        assertThatCode(() -> MEMBER_ZED.requireAllowed(true)).doesNotThrowAnyException();
        assertThatThrownBy(() -> MEMBER_ZED.requireAllowed(false)).isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> Actor.anonymous().requireAllowed(false)).isInstanceOf(SignInRequiredException.class);
    }

    @Test
    void tellsWhoMayChangeAnEntry() {
        assertThat(new AddedBy(ZED).mayBeChangedBy(MEMBER_ZED)).isTrue();
        assertThat(new AddedBy(ZED).mayBeChangedBy(ADMIN_AMY)).isTrue();
        assertThat(new AddedBy(ZED).mayBeChangedBy(MEMBER_AMY)).isFalse();
        assertThat(new AddedBy(ZED).mayBeChangedBy(Actor.anonymous())).isFalse();
        assertThat(AddedBy.NOBODY.mayBeChangedBy(MEMBER_ZED)).isFalse();
        assertThat(AddedBy.NOBODY.mayBeChangedBy(ADMIN_AMY)).isTrue();
    }

    @Test
    void anEntryInsideAnotherIsAlsoChangedByWhoeverAddedTheOther() {
        assertThat(new AddedBy(AMY).mayBeChangedBy(MEMBER_ZED, new AddedBy(ZED)))
                .isTrue();
        assertThat(new AddedBy(ZED).mayBeChangedBy(MEMBER_ZED, new AddedBy(AMY)))
                .isTrue();
        assertThat(new AddedBy(AMY).mayBeChangedBy(MEMBER_ZED, AddedBy.NOBODY)).isFalse();
        assertThat(AddedBy.NOBODY.mayBeChangedBy(ADMIN_AMY, AddedBy.NOBODY)).isTrue();
        assertThat(AddedBy.NOBODY.mayBeChangedBy(Actor.anonymous(), AddedBy.NOBODY))
                .isFalse();
    }

    @Test
    void tellsWhatAnActorMayDo() {
        assertThat(MEMBER_ZED.mayAddEntries()).isTrue();
        assertThat(MEMBER_ZED.mayKeepCategories()).isFalse();
        assertThat(ADMIN_AMY.mayKeepCategories()).isTrue();
        assertThat(Actor.anonymous().mayAddEntries()).isFalse();
        assertThat(Actor.anonymous().mayKeepCategories()).isFalse();
    }
}
