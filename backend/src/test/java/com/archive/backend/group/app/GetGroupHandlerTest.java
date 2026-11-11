package com.archive.backend.group.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.read.GroupDetails;
import com.archive.backend.group.read.GroupLabel;
import com.archive.backend.shared.domain.error.NotFoundException;
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
                "Crew",
                "Crew Ltd",
                null,
                List.of(),
                List.of(new GroupLabel(UUID.randomUUID(), "Crew", true)),
                List.of(),
                List.of(),
                List.of(),
                List.of());
        reads.add(group);

        assertThat(handler.handle(new GetGroupQuery(new GroupId(ID)))).isEqualTo(group);
    }

    @Test
    void failsWhenTheGroupDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new GetGroupQuery(new GroupId(ID))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("group.not-found"));
    }
}
