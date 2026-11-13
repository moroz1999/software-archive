package com.archive.backend.group.app;

import static com.archive.backend.group.app.GroupFixtures.CREW;
import static com.archive.backend.group.app.GroupFixtures.crew;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.user.domain.ChangeForbiddenException;
import org.junit.jupiter.api.Test;

class DeleteGroupHandlerTest {

    private final InMemoryGroupRepository repository = new InMemoryGroupRepository();
    private final DeleteGroupHandler handler = new DeleteGroupHandler(repository, event -> {});

    @Test
    void removesTheGroup() {
        repository.save(crew());

        handler.handle(new DeleteGroupCommand(ADMIN, CREW));

        assertThat(repository.findById(CREW)).isEmpty();
    }

    @Test
    void failsWhenTheGroupDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new DeleteGroupCommand(ADMIN, CREW)))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("group.not-found"));
    }

    @Test
    void refusesSomeoneWhoMayNotChangeTheGroupAndKeepsIt() {
        repository.save(crew());

        assertThatThrownBy(() -> handler.handle(new DeleteGroupCommand(MEMBER, CREW)))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(repository.findById(CREW)).isPresent();
    }
}
