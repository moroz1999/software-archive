package com.archive.backend.category.domain;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import org.junit.jupiter.api.Test;

/** Categories are kept by administrators only (docs/domain/users.md). */
class CategoryPermissionsTest {

    @Test
    void areKeptByAdministrators() {
        assertThatCode(() -> Category.requireKeptBy(ADMIN)).doesNotThrowAnyException();
    }

    @Test
    void areNotKeptByMembers() {
        assertThatThrownBy(() -> Category.requireKeptBy(MEMBER)).isInstanceOf(ChangeForbiddenException.class);
    }

    @Test
    void tellAPublicVisitorToSignIn() {
        assertThatThrownBy(() -> Category.requireKeptBy(Actor.anonymous())).isInstanceOf(SignInRequiredException.class);
    }
}
