package com.archive.backend.author.app;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.read.AuthorLabel;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.shared.domain.error.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetAuthorHandlerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final InMemoryAuthorReadRepository reads = new InMemoryAuthorReadRepository();
    private final GetAuthorHandler handler = new GetAuthorHandler(reads);

    @Test
    void returnsTheAuthor() {
        AuthorDetails author = new AuthorDetails(ID, "Zed", "John Doe", null, List.of(new AuthorLabel(UUID.randomUUID(), "Zed")), List.of(), List.of());
        reads.add(author);

        assertThat(handler.handle(new GetAuthorQuery(new AuthorId(ID)))).isEqualTo(author);
    }

    @Test
    void failsWhenTheAuthorDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new GetAuthorQuery(new AuthorId(ID))))
                .isInstanceOfSatisfying(NotFoundException.class, e -> assertThat(e.code()).isEqualTo("author.not-found"));
    }
}
