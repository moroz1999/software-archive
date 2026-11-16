package com.archive.backend.author.read;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthorCatalogEntryTest {

    @Test
    void isOfAPersonOrALabelNeverBoth() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> new AuthorCatalogEntry(id, id, "Ghost", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isOfAPersonOrALabelNeverNeither() {
        assertThatThrownBy(() -> new AuthorCatalogEntry(null, null, "Ghost", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
