package com.archive.backend.user.app;

import static com.archive.backend.user.app.UserFixtures.NOW;
import static com.archive.backend.user.app.UserFixtures.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserNotFoundException;
import com.archive.backend.user.read.CurrentUser;
import com.archive.backend.user.read.UserEntry;
import com.archive.backend.user.read.UserReadRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

class GetUserHandlerTest {

    private static final Actor ADMIN = new Actor(id(1), Set.of(UserGroup.MEMBER, UserGroup.ADMIN));
    private static final Actor MEMBER = new Actor(id(2), Set.of(UserGroup.MEMBER));
    private static final UserEntry ZED =
            new UserEntry(id(2).value(), "Zed", "zed@example.com", true, NOW, List.of("member"));

    private final GetUserHandler handler = new GetUserHandler(new ZedOnly());

    @Test
    void anAdministratorReadsAUser() {
        assertThat(handler.handle(new GetUserQuery(ADMIN, id(2)))).isEqualTo(ZED);
    }

    @Test
    void anUnknownUserIsNotFound() {
        assertThatThrownBy(() -> handler.handle(new GetUserQuery(ADMIN, id(9))))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void aMemberReadsNobody() {
        assertThatThrownBy(() -> handler.handle(new GetUserQuery(MEMBER, id(2))))
                .isInstanceOf(ChangeForbiddenException.class);
    }

    @Test
    void aPublicVisitorIsToldToSignIn() {
        assertThatThrownBy(() -> handler.handle(new GetUserQuery(Actor.anonymous(), id(2))))
                .isInstanceOf(SignInRequiredException.class);
    }

    /** Knows Zed only. */
    private static final class ZedOnly implements UserReadRepository {

        @Override
        public Optional<CurrentUser> findCurrent(UUID id) {
            return Optional.empty();
        }

        @Override
        public Optional<UserEntry> findEntry(UUID id) {
            return ZED.id().equals(id) ? Optional.of(ZED) : Optional.empty();
        }

        @Override
        public Page<UserEntry> findPage(@Nullable String search, Pageable pageable) {
            return Page.empty(pageable);
        }
    }
}
