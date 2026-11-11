package com.archive.backend.category.infra;

import static com.archive.backend.category.infra.SeededTree.DEMOSCENE;
import static com.archive.backend.category.infra.SeededTree.GAMES;
import static com.archive.backend.category.infra.SeededTree.INTRO;
import static com.archive.backend.category.infra.SeededTree.INTRO_4K;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.category.domain.CategoryId;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

/** Reads lines of the tree the schema brings. */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcCategoryHierarchy.class})
class JdbcCategoryHierarchyIT {

    @Autowired
    private JdbcCategoryHierarchy hierarchy;

    @Test
    void readsTheLineOfACategoryUpToTheTop() {
        assertThat(hierarchy.lineOf(new CategoryId(INTRO_4K)))
                .hasValue(
                        java.util.List.of(new CategoryId(INTRO_4K), new CategoryId(INTRO), new CategoryId(DEMOSCENE)));
    }

    @Test
    void readsACategoryAtTheTopAsALineOfItsOwn() {
        assertThat(hierarchy.lineOf(new CategoryId(GAMES))).hasValue(java.util.List.of(new CategoryId(GAMES)));
    }

    @Test
    void knowsNoCategoryOutsideTheArchive() {
        assertThat(hierarchy.lineOf(new CategoryId(UUID.fromString("00000000-0000-0000-0000-000000000009"))))
                .isEmpty();
    }
}
