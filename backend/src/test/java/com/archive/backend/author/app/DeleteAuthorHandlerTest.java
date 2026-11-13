package com.archive.backend.author.app;

import static com.archive.backend.author.app.AuthorFixtures.ZED;
import static com.archive.backend.author.app.AuthorFixtures.zed;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.user.domain.ChangeForbiddenException;
import org.junit.jupiter.api.Test;

class DeleteAuthorHandlerTest {

    private final InMemoryAuthorRepository repository = new InMemoryAuthorRepository();
    private final DeleteAuthorHandler handler = new DeleteAuthorHandler(repository, event -> {});

    @Test
    void removesTheAuthor() {
        repository.save(zed());

        handler.handle(new DeleteAuthorCommand(ADMIN, ZED));

        assertThat(repository.findById(ZED)).isEmpty();
    }

    @Test
    void failsWhenTheAuthorDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new DeleteAuthorCommand(ADMIN, ZED)))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("author.not-found"));
    }

    @Test
    void refusesSomeoneWhoMayNotChangeThePersonAndKeepsThem() {
        repository.save(zed());

        assertThatThrownBy(() -> handler.handle(new DeleteAuthorCommand(MEMBER, ZED)))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(repository.findById(ZED)).isPresent();
    }
}
