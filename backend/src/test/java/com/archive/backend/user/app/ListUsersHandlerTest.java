package com.archive.backend.user.app;

import static com.archive.backend.user.app.UserFixtures.NOW;
import static com.archive.backend.user.app.UserFixtures.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserSearch;
import com.archive.backend.user.read.CurrentUser;
import com.archive.backend.user.read.UserEntry;
import com.archive.backend.user.read.UserReadRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class ListUsersHandlerTest {

    private static final Actor ADMIN = new Actor(id(1), Set.of(UserGroup.MEMBER, UserGroup.ADMIN));
    private static final Actor MEMBER = new Actor(id(2), Set.of(UserGroup.MEMBER));
    private static final UserEntry ZED =
            new UserEntry(id(2).value(), "Zed", "zed@example.com", true, NOW, List.of("member"));

    private final RecordingReads reads = new RecordingReads();
    private final ListUsersHandler handler = new ListUsersHandler(reads);

    @Test
    void anAdministratorReadsAPageOfUsersWithTheirSearch() {
        Page<UserEntry> page = handler.handle(new ListUsersQuery(ADMIN, new UserSearch("ze"), PageRequest.of(1, 50)));

        assertThat(page.getContent()).containsExactly(ZED);
        assertThat(reads.asked).containsExactly("ze @ " + PageRequest.of(1, 50));
    }

    @Test
    void anAdministratorReadsEveryUserWithoutASearch() {
        handler.handle(new ListUsersQuery(ADMIN, null, PageRequest.of(0, 50)));

        assertThat(reads.asked).containsExactly("null @ " + PageRequest.of(0, 50));
    }

    @Test
    void aMemberSeesNoUsers() {
        assertThatThrownBy(() -> handler.handle(new ListUsersQuery(MEMBER, null, PageRequest.of(0, 50))))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(reads.asked).isEmpty();
    }

    @Test
    void aPublicVisitorIsToldToSignIn() {
        assertThatThrownBy(() -> handler.handle(new ListUsersQuery(Actor.anonymous(), null, PageRequest.of(0, 50))))
                .isInstanceOf(SignInRequiredException.class);
    }

    /** Answers one page of Zed and remembers what it was asked. */
    private static final class RecordingReads implements UserReadRepository {

        final List<String> asked = new ArrayList<>();

        @Override
        public Optional<CurrentUser> findCurrent(UUID id) {
            return Optional.empty();
        }

        @Override
        public Page<UserEntry> findPage(@Nullable String search, Pageable pageable) {
            asked.add(search + " @ " + pageable);
            return new PageImpl<>(List.of(ZED), pageable, 1);
        }
    }
}
