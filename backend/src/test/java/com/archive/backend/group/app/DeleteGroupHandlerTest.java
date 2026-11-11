package com.archive.backend.group.app;

import static com.archive.backend.group.app.GroupFixtures.CREW;
import static com.archive.backend.group.app.GroupFixtures.crew;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.NotFoundException;
import org.junit.jupiter.api.Test;

class DeleteGroupHandlerTest {

    private final InMemoryGroupRepository repository = new InMemoryGroupRepository();
    private final DeleteGroupHandler handler = new DeleteGroupHandler(repository);

    @Test
    void removesTheGroup() {
        repository.save(crew());

        handler.handle(new DeleteGroupCommand(CREW));

        assertThat(repository.findById(CREW)).isEmpty();
    }

    @Test
    void failsWhenTheGroupDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new DeleteGroupCommand(CREW)))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("group.not-found"));
    }
}
