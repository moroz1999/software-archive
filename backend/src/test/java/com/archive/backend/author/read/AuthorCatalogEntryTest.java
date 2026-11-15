package com.archive.backend.author.read;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthorCatalogEntryTest {

    @Test
    void isOfAPersonOrALabelOfNobodyNeverBoth() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> new AuthorCatalogEntry(id, "Ghost", null, null, id))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isOfAPersonOrALabelOfNobodyNeverNeither() {
        assertThatThrownBy(() -> new AuthorCatalogEntry(null, "Ghost", null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
