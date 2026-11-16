package com.archive.backend.group.domain;

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
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Who may change a group (docs/domain/users.md). */
class GroupPermissionsTest {

    private static Group addedBy(AddedBy addedBy) {
        return Group.create(
                new GroupId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                new GroupName("Crew Ltd"),
                null,
                GroupTypeSet.restore(Set.of(new GroupTypeCode("demogroup"))),
                List.of(),
                new Locations(List.of()),
                GroupMembers.EMPTY,
                addedBy);
    }

    @Test
    void remembersWhoAddedIt() {
        assertThat(addedBy(new AddedBy(MEMBER_ID)).getAddedBy()).isEqualTo(new AddedBy(MEMBER_ID));
    }

    @Test
    void isChangedByWhoeverAddedItAndByAdministrators() {
        Group group = addedBy(new AddedBy(MEMBER_ID));

        assertThatCode(() -> group.requireChangeableBy(MEMBER)).doesNotThrowAnyException();
        assertThatCode(() -> group.requireChangeableBy(ADMIN)).doesNotThrowAnyException();
        assertThatThrownBy(() -> group.requireChangeableBy(OTHER)).isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> group.requireChangeableBy(Actor.anonymous()))
                .isInstanceOf(SignInRequiredException.class);
    }

    @Test
    void whoseAdderAccountIsDeletedIsChangedByAdministratorsOnly() {
        assertThatThrownBy(() -> addedBy(AddedBy.ACCOUNT_DELETED).requireChangeableBy(MEMBER))
                .isInstanceOf(ChangeForbiddenException.class);
    }
}
