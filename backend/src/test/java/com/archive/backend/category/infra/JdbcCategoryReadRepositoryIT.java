package com.archive.backend.category.infra;

import static com.archive.backend.category.infra.SeededTree.DEMOSCENE;
import static com.archive.backend.category.infra.SeededTree.GAMES;
import static com.archive.backend.category.infra.SeededTree.INTRO;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.category.read.CategoryDetails;
import com.archive.backend.category.read.CategoryNode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** Reads the tree the schema brings: the categories of zxart, titled in English and in Russian. */
@IntegrationTest
@Transactional
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
        List<CategoryNode> tree = reads.findTree("en");

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
    void readsTheTreeInRussian() {
        List<CategoryNode> tree = reads.findTree("ru");

        assertThat(tree)
                .extracting(CategoryNode::title)
                .containsExactly(
                        "Игры",
                        "Системное программное обеспечение",
                        "Разное",
                        "Образовательные",
                        "Сборники",
                        "Демосцена",
                        "Пресса",
                        "Прикладное ПО",
                        "Серии");
        assertThat(child(tree.get(5), "Интро").children().get(8).title()).isEqualTo("4K интро");
    }

    @Test
    void titlesEveryCategoryOfZxartInRussian() {
        Integer untitled = jdbc.queryForObject(
                "SELECT count(*) FROM category c WHERE NOT EXISTS"
                        + " (SELECT 1 FROM category_title t WHERE t.category_id = c.id AND t.language = 'ru')",
                Integer.class);

        assertThat(untitled).isZero();
    }

    @Test
    void readsACategoryAddedLastAtTheEndOfItsParent() {
        UUID added = UUID.fromString("00000000-0000-0000-0000-000000000001");
        insert(added, INTRO, 15, "64K интро", "64K intro");

        List<CategoryNode> intros = child(reads.findTree("en").get(5), "Intro").children();

        assertThat(intros).hasSize(16);
        assertThat(intros.getLast()).isEqualTo(new CategoryNode(added, "64K intro", List.of()));
    }

    @Test
    void titlesACategoryWithoutATitleInTheLanguageAskedForInRussian() {
        UUID added = UUID.fromString("00000000-0000-0000-0000-000000000001");
        insert(added, INTRO, 15, "64K интро", null);

        List<CategoryNode> intros = child(reads.findTree("en").get(5), "Intro").children();

        assertThat(intros.getLast()).isEqualTo(new CategoryNode(added, "64K интро", List.of()));
    }

    @Test
    void readsACategoryWithItsParentAndItsTitlesInEveryLanguage() {
        assertThat(reads.findById(INTRO))
                .hasValue(new CategoryDetails(INTRO, Map.of("en", "Intro", "ru", "Интро"), DEMOSCENE));
    }

    @Test
    void readsACategoryAtTheTopWithoutAParent() {
        assertThat(reads.findById(GAMES))
                .hasValue(new CategoryDetails(GAMES, Map.of("en", "Games", "ru", "Игры"), null));
    }

    @Test
    void readsOnlyTheTitlesACategoryHas() {
        UUID added = UUID.fromString("00000000-0000-0000-0000-000000000001");
        insert(added, INTRO, 15, "64K интро", null);

        assertThat(reads.findById(added)).hasValue(new CategoryDetails(added, Map.of("ru", "64K интро"), INTRO));
    }

    private void insert(UUID id, UUID parent, int position, String russian, @Nullable String english) {
        jdbc.update("INSERT INTO category (id, parent_id, position) VALUES (?, ?, ?)", id, parent, position);
        jdbc.update("INSERT INTO category_title (category_id, language, title) VALUES (?, 'ru', ?)", id, russian);
        if (english != null) {
            jdbc.update("INSERT INTO category_title (category_id, language, title) VALUES (?, 'en', ?)", id, english);
        }
    }

    @Test
    void readsNothingForAnUnknownCategory() {
        assertThat(reads.findById(UUID.fromString("00000000-0000-0000-0000-000000000009")))
                .isEmpty();
    }
}
