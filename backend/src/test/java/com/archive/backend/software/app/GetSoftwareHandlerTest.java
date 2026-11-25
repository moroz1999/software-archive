package com.archive.backend.software.app;

import static com.archive.backend.user.TestActors.ADDED;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static com.archive.backend.user.TestActors.OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareHardware;
import com.archive.backend.software.read.SoftwarePlatform;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetSoftwareHandlerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final InMemorySoftwareReadRepository reads = new InMemorySoftwareReadRepository();
    private final GetSoftwareHandler handler = new GetSoftwareHandler(reads);

    @Test
    void returnsTheSoftware() {
        SoftwareDetails details = new SoftwareDetails(
                ID,
                "Elite",
                "Space trading",
                "1985-09-20",
                new SoftwarePlatform("vector", "Vector"),
                List.of(new SoftwareHardware("vector06c", "Vector-06C", "computers", "Computers", 1)),
                List.of(),
                ADDED);
        reads.add(details);

        assertThat(handler.handle(new GetSoftwareQuery(Actor.anonymous(), new SoftwareId(ID), Language.EN)))
                .isEqualTo(details);
    }

    @Test
    void failsWhenTheSoftwareDoesNotExist() {
        assertThatThrownBy(
                        () -> handler.handle(new GetSoftwareQuery(Actor.anonymous(), new SoftwareId(ID), Language.EN)))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }

    @Test
    void tellsWhetherTheActorMayChangeTheSoftware() {
        reads.add(new SoftwareDetails(
                ID,
                "Elite",
                null,
                null,
                "unknown",
                new SoftwarePlatform("vector", "Vector"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                ADDED,
                MEMBER_ID.value(),
                false));

        assertThat(handler.handle(new GetSoftwareQuery(MEMBER, new SoftwareId(ID), Language.EN))
                        .canChange())
                .isTrue();
        assertThat(handler.handle(new GetSoftwareQuery(ADMIN, new SoftwareId(ID), Language.EN))
                        .canChange())
                .isTrue();
        assertThat(handler.handle(new GetSoftwareQuery(OTHER, new SoftwareId(ID), Language.EN))
                        .canChange())
                .isFalse();
        assertThat(handler.handle(new GetSoftwareQuery(Actor.anonymous(), new SoftwareId(ID), Language.EN))
                        .canChange())
                .isFalse();
    }
}
