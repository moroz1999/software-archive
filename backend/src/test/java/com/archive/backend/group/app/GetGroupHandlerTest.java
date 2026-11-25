package com.archive.backend.group.app;

import static com.archive.backend.user.TestActors.ADDED;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static com.archive.backend.user.TestActors.OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.read.GroupDetails;
import com.archive.backend.group.read.GroupLabel;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetGroupHandlerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final InMemoryGroupReadRepository reads = new InMemoryGroupReadRepository();
    private final GetGroupHandler handler = new GetGroupHandler(reads);

    @Test
    void returnsTheGroup() {
        GroupDetails group = new GroupDetails(
                ID,
                "Crew Ltd",
                null,
                List.of(),
                List.of(new GroupLabel(UUID.randomUUID(), "Crew")),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                ADDED);
        reads.add(group);

        assertThat(handler.handle(new GetGroupQuery(Actor.anonymous(), new GroupId(ID), Language.EN)))
                .isEqualTo(group);
    }

    @Test
    void failsWhenTheGroupDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new GetGroupQuery(Actor.anonymous(), new GroupId(ID), Language.EN)))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("group.not-found"));
    }

    @Test
    void tellsWhetherTheActorMayChangeTheGroup() {
        reads.add(new GroupDetails(
                ID,
                "Crew Ltd",
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                ADDED,
                MEMBER_ID.value(),
                false));

        assertThat(handler.handle(new GetGroupQuery(MEMBER, new GroupId(ID), Language.EN))
                        .canChange())
                .isTrue();
        assertThat(handler.handle(new GetGroupQuery(ADMIN, new GroupId(ID), Language.EN))
                        .canChange())
                .isTrue();
        assertThat(handler.handle(new GetGroupQuery(OTHER, new GroupId(ID), Language.EN))
                        .canChange())
                .isFalse();
        assertThat(handler.handle(new GetGroupQuery(Actor.anonymous(), new GroupId(ID), Language.EN))
                        .canChange())
                .isFalse();
    }
}
