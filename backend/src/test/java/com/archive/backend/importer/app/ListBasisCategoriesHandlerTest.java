package com.archive.backend.importer.app;

import static com.archive.backend.user.TestActors.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ListBasisCategoriesHandlerTest {

    private final ListBasisCategoriesHandler handler = new ListBasisCategoriesHandler();

    @Test
    void givesTheCategoriesStoredInTheArchiveWithoutReadingTheSource() {
        assertThat(handler.handle(new ListBasisCategoriesQuery(ADMIN)))
                .contains("arcade", "demo", "ay", "disk", "3D")
                .doesNotContain("pc", "magazine", "document", "catalogue");
    }
}
