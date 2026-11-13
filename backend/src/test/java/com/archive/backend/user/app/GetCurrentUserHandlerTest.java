package com.archive.backend.user.app;

import static com.archive.backend.user.app.UserFixtures.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.AuthenticationRequiredException;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.session.RefreshSecret;
import com.archive.backend.user.read.CurrentUser;
import com.archive.backend.user.read.UserEntry;
import com.archive.backend.user.read.UserReadRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

class GetCurrentUserHandlerTest {

    private static final CurrentUser ZED =
            new CurrentUser(id(1).value(), "Zed", "zed@example.com", List.of("admin", "member"));

    private final Map<UUID, CurrentUser> stored = Map.of(ZED.id(), ZED);
    private final UserReadRepository repository = new UserReadRepository() {
        @Override
        public Optional<CurrentUser> findCurrent(UUID userId) {
            return Optional.ofNullable(stored.get(userId));
        }

        @Override
        public Page<UserEntry> findPage(@Nullable String search, Pageable pageable) {
            return Page.empty(pageable);
        }
    };
    private final GetCurrentUserHandler handler = new GetCurrentUserHandler(repository);

    @Test
    void tellsTheSignedInUser() {
        assertThat(handler.handle(new GetCurrentUserQuery(new Actor(id(1), Set.of(UserGroup.MEMBER)), null)))
                .contains(ZED.withAbilities(List.of("add-entries")));
    }

    @Test
    void aPublicVisitorIsNobody() {
        assertThat(handler.handle(new GetCurrentUserQuery(Actor.anonymous(), null)))
                .isEmpty();
    }

    @Test
    void aDeletedAccountIsNobody() {
        assertThat(handler.handle(new GetCurrentUserQuery(new Actor(id(2), Set.of(UserGroup.MEMBER)), null)))
                .isEmpty();
    }

    @Test
    void aVisitorWithASessionToRefreshIsToldToRefreshIt() {
        assertThatThrownBy(
                        () -> handler.handle(new GetCurrentUserQuery(Actor.anonymous(), new RefreshSecret("secret"))))
                .isInstanceOfSatisfying(
                        AuthenticationRequiredException.class,
                        e -> assertThat(e.code()).isEqualTo("auth.required"));
    }

    @Test
    void tellsWhatAMemberMayDo() {
        assertThat(handler.handle(new GetCurrentUserQuery(new Actor(id(1), Set.of(UserGroup.MEMBER)), null)))
                .hasValueSatisfying(user -> assertThat(user.abilities()).containsExactly("add-entries"));
    }

    @Test
    void tellsWhatAnAdministratorMayDo() {
        assertThat(handler.handle(
                        new GetCurrentUserQuery(new Actor(id(1), Set.of(UserGroup.MEMBER, UserGroup.ADMIN)), null)))
                .hasValueSatisfying(
                        user -> assertThat(user.abilities()).containsExactly("add-entries", "keep-categories"));
    }
}
