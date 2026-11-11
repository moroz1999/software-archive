package com.archive.backend.category.infra;

import static com.archive.backend.category.infra.SeededTree.DEMOSCENE;
import static com.archive.backend.category.infra.SeededTree.GAMES;
import static com.archive.backend.category.infra.SeededTree.INTRO;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.category.read.CategoryDetails;
import com.archive.backend.category.read.CategoryNode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/** Reads the tree the schema brings: the categories of zxart. */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcCategoryReadRepository.class})
class JdbcCategoryReadRepositoryIT {

    @Autowired
    private JdbcCategoryReadRepository reads;

    @Autowired
    private JdbcTemplate jdbc;

    private static Stream<CategoryNode> flatten(CategoryNode node) {
        return Stream.concat(Stream.of(node), node.children().stream().flatMap(JdbcCategoryReadRepositoryIT::flatten));
    }

    private static CategoryNode child(CategoryNode parent, String title) {
        return parent.children().stream()
                .filter(node -> node.title().equals(title))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void readsTheWholeTreeInItsOrder() {
        List<CategoryNode> tree = reads.findTree();

        assertThat(tree)
                .extracting(CategoryNode::title)
                .containsExactly(
                        "Games",
                        "System Software",
                        "Misc",
                        "Educational",
                        "Compilation",
                        "Demoscene",
                        "Press",
                        "Applications",
                        "Series");
        assertThat(tree.stream().flatMap(JdbcCategoryReadRepositoryIT::flatten)).hasSize(288);
        assertThat(child(tree.get(5), "Intro").children())
                .extracting(CategoryNode::title)
                .containsExactly(
                        "8b intro",
                        "16b intro",
                        "32b intro",
                        "64b intro",
                        "128b intro",
                        "256b intro",
                        "512b intro",
                        "1K intro",
                        "4K intro",
                        "8K intro",
                        "16K intro",
                        "32K intro",
                        "128k intro",
                        "BBStro",
                        "Cracktro");
        assertThat(child(
                                child(child(child(tree.getFirst(), "Action"), "Shooters"), "Shoot 'em up (Shmups)"),
                                "Horizontal Shmups")
                        .children())
                .extracting(CategoryNode::title)
                .containsExactly("Scramble Shooters", "Defender shooters");
    }

    @Test
    void readsACategoryAddedLastAtTheEndOfItsParent() {
        UUID added = UUID.fromString("00000000-0000-0000-0000-000000000001");
        jdbc.update(
                "INSERT INTO category (id, parent_id, title, position) VALUES (?, ?, '64K intro', 15)", added, INTRO);

        List<CategoryNode> intros = child(reads.findTree().get(5), "Intro").children();

        assertThat(intros).hasSize(16);
        assertThat(intros.getLast()).isEqualTo(new CategoryNode(added, "64K intro", List.of()));
    }

    @Test
    void readsACategoryWithItsParent() {
        assertThat(reads.findById(INTRO)).hasValue(new CategoryDetails(INTRO, "Intro", DEMOSCENE));
    }

    @Test
    void readsACategoryAtTheTopWithoutAParent() {
        assertThat(reads.findById(GAMES)).hasValue(new CategoryDetails(GAMES, "Games", null));
    }

    @Test
    void readsNothingForAnUnknownCategory() {
        assertThat(reads.findById(UUID.fromString("00000000-0000-0000-0000-000000000009")))
                .isEmpty();
    }
}
