package com.archive.backend.author.app;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static com.archive.backend.user.TestActors.OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.author.read.AuthorLabel;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetAuthorHandlerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final InMemoryAuthorReadRepository reads = new InMemoryAuthorReadRepository();
    private final GetAuthorHandler handler = new GetAuthorHandler(reads);

    @Test
    void returnsTheAuthor() {
        AuthorDetails author = new AuthorDetails(
                ID,
                "Zed",
                "John Doe",
                null,
                List.of(new AuthorLabel(UUID.randomUUID(), "Zed", true)),
                List.of(),
                List.of());
        reads.add(author);

        assertThat(handler.handle(new GetAuthorQuery(Actor.anonymous(), new AuthorId(ID))))
                .isEqualTo(author);
    }

    @Test
    void failsWhenTheAuthorDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new GetAuthorQuery(Actor.anonymous(), new AuthorId(ID))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("author.not-found"));
    }

    @Test
    void tellsWhetherTheActorMayChangeThePerson() {
        reads.add(new AuthorDetails(ID, "Zed", null, null, List.of(), List.of(), List.of(), MEMBER_ID.value(), false));

        assertThat(handler.handle(new GetAuthorQuery(MEMBER, new AuthorId(ID))).canChange())
                .isTrue();
        assertThat(handler.handle(new GetAuthorQuery(ADMIN, new AuthorId(ID))).canChange())
                .isTrue();
        assertThat(handler.handle(new GetAuthorQuery(OTHER, new AuthorId(ID))).canChange())
                .isFalse();
        assertThat(handler.handle(new GetAuthorQuery(Actor.anonymous(), new AuthorId(ID)))
                        .canChange())
                .isFalse();
    }
}
