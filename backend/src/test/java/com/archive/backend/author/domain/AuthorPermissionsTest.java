package com.archive.backend.author.domain;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static com.archive.backend.user.TestActors.OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.geo.domain.Locations;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.AddedBy;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Who may change a person (docs/domain/users.md). */
class AuthorPermissionsTest {

    private static Author addedBy(AddedBy addedBy) {
        return Author.create(
                new AuthorId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                new RealName("Zed"),
                null,
                List.of(),
                new Locations(List.of()),
                addedBy);
    }

    @Test
    void remembersWhoAddedThem() {
        assertThat(addedBy(new AddedBy(MEMBER_ID)).getAddedBy()).isEqualTo(new AddedBy(MEMBER_ID));
    }

    @Test
    void isChangedByWhoeverAddedThemAndByAdministrators() {
        Author author = addedBy(new AddedBy(MEMBER_ID));

        assertThatCode(() -> author.requireChangeableBy(MEMBER)).doesNotThrowAnyException();
        assertThatCode(() -> author.requireChangeableBy(ADMIN)).doesNotThrowAnyException();
        assertThatThrownBy(() -> author.requireChangeableBy(OTHER)).isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> author.requireChangeableBy(Actor.anonymous()))
                .isInstanceOf(SignInRequiredException.class);
    }

    @Test
    void ofNobodyIsChangedByAdministratorsOnly() {
        assertThatThrownBy(() -> addedBy(AddedBy.NOBODY).requireChangeableBy(MEMBER))
                .isInstanceOf(ChangeForbiddenException.class);
    }
}
