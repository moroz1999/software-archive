package com.archive.backend.search.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.archive.backend.search.TypesenseProbe;
import com.archive.backend.search.domain.SearchCollection;
import com.archive.backend.search.domain.SearchEntityType;
import com.archive.backend.search.domain.SoftwareDocument;
import com.archive.backend.search.read.IdCount;
import com.archive.backend.search.read.SoftwareCatalogFilter;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.search.read.SoftwareCatalogPage;
import com.archive.backend.search.read.YearCount;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.typesense.TypesenseContainer;

/**
 * The software catalog read from Typesense (docs/domain/search.md#software-catalog): every test puts a collection of its
 * own in use, holding exactly the software below.
 */
@SpringBootTest
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class})
class TypesenseSoftwareCatalogIT {

    private static final UUID GAMES = UUID.fromString("00000000-0000-0000-00fd-0000000000c1");
    private static final UUID ACTION = UUID.fromString("00000000-0000-0000-00fd-0000000000c2");
    private static final UUID SHOOTERS = UUID.fromString("00000000-0000-0000-00fd-0000000000c3");
    private static final UUID PUZZLE = UUID.fromString("00000000-0000-0000-00fd-0000000000c4");
    private static final UUID DEMOSCENE = UUID.fromString("00000000-0000-0000-00fd-0000000000c5");

    private static final UUID FINLAND = UUID.fromString("00000000-0000-0000-00fd-0000000000f1");
    private static final UUID BRITAIN = UUID.fromString("00000000-0000-0000-00fd-0000000000f2");

    private static final SoftwareDocument ELITE = software(
            1,
            "Elite",
            List.of("Элита"),
            "vector",
            LocalDate.of(1987, 5, 1),
            List.of(SHOOTERS, ACTION, GAMES),
            List.of(FINLAND));
    private static final SoftwareDocument EXOLON = software(
            2, "Exolon", List.of(), "vector", LocalDate.of(1987, 9, 1), List.of(ACTION, GAMES), List.of(BRITAIN));
    private static final SoftwareDocument TETRIS = software(
            3, "tetris", List.of(), "vector", LocalDate.of(1989, 1, 1), List.of(PUZZLE, GAMES), List.of(FINLAND));
    private static final SoftwareDocument DEMO =
            software(4, "Demo", List.of(), "vector", null, List.of(DEMOSCENE), List.of());
    private static final SoftwareDocument BOLDER = software(
            5, "Bolder Dash", List.of(), "bk", LocalDate.of(1987, 1, 1), List.of(PUZZLE, GAMES), List.of(FINLAND));

    @Autowired
    private TypesenseSoftwareCatalog catalog;

    @Autowired
    private TypesenseSearchIndex index;

    @Autowired
    private TypesenseContainer typesense;

    private static SoftwareDocument software(
            int n,
            String title,
            List<String> alternativeTitles,
            String platform,
            @Nullable LocalDate releaseDate,
            List<UUID> categories,
            List<UUID> countries) {
        return new SoftwareDocument(
                UUID.fromString("00000000-0000-0000-00fd-00000000000" + n),
                title,
                releaseDate,
                alternativeTitles,
                platform,
                releaseDate == null ? null : releaseDate.getYear(),
                categories,
                countries);
    }

    @BeforeEach
    void putTheSoftwareInUse() {
        SearchCollection collection = index.create(SearchEntityType.SOFTWARE);
        index.load(collection, List.of(ELITE, EXOLON, TETRIS, DEMO, BOLDER));
        index.switchTo(collection);
    }

    @AfterEach
    void resume() {
        new TypesenseProbe(typesense).resume();
    }

    private SoftwareCatalogPage find(SoftwareCatalogFilter filter) {
        return catalog.find(filter, PageRequest.of(0, 20));
    }

    private static SoftwareCatalogFilter vector() {
        return new SoftwareCatalogFilter(null, "vector", null, null, null);
    }

    private static List<String> titles(SoftwareCatalogPage page) {
        return page.items().getContent().stream()
                .map(SoftwareCatalogItem::title)
                .toList();
    }

    @Test
    void listsTheSoftwareOfThePlatformByTitleWhateverTheCase() {
        SoftwareCatalogPage page = find(vector());

        assertThat(titles(page)).containsExactly("Demo", "Elite", "Exolon", "tetris");
        assertThat(page.items().getTotalElements()).isEqualTo(4);
    }

    @Test
    void listsTheSoftwareOfEveryPlatformWithoutOne() {
        assertThat(titles(find(new SoftwareCatalogFilter(null, null, null, null, null))))
                .containsExactly("Bolder Dash", "Demo", "Elite", "Exolon", "tetris");
    }

    @Test
    void givesTheCardOfTheSoftware() {
        SoftwareCatalogPage page = find(new SoftwareCatalogFilter("Elite", "vector", null, null, null));

        assertThat(page.items().getContent())
                .containsExactly(new SoftwareCatalogItem(ELITE.id(), "Elite", LocalDate.of(1987, 5, 1)));
        assertThat(find(new SoftwareCatalogFilter("Demo", "vector", null, null, null))
                        .items()
                        .getContent())
                .containsExactly(new SoftwareCatalogItem(DEMO.id(), "Demo", null));
    }

    @Test
    void findsSoftwareByTheBeginningOfItsTitleOrOfAnAlternativeTitle() {
        assertThat(titles(find(new SoftwareCatalogFilter("tetr", "vector", null, null, null))))
                .containsExactly("tetris");
        assertThat(titles(find(new SoftwareCatalogFilter("Элит", "vector", null, null, null))))
                .containsExactly("Elite");
    }

    @Test
    void narrowsByTheYear() {
        assertThat(titles(find(new SoftwareCatalogFilter(null, "vector", 1987, null, null))))
                .containsExactly("Elite", "Exolon");
    }

    @Test
    void aCategoryFindsTheSoftwareOfItsWholeSubtree() {
        assertThat(titles(find(new SoftwareCatalogFilter(null, "vector", null, GAMES, null))))
                .containsExactly("Elite", "Exolon", "tetris");
        assertThat(titles(find(new SoftwareCatalogFilter(null, "vector", null, SHOOTERS, null))))
                .containsExactly("Elite");
    }

    @Test
    void narrowsByTheCountry() {
        assertThat(titles(find(new SoftwareCatalogFilter(null, "vector", null, null, FINLAND))))
                .containsExactly("Elite", "tetris");
    }

    @Test
    void givesThePageAskedFor() {
        SoftwareCatalogPage page = catalog.find(vector(), PageRequest.of(1, 2));

        assertThat(titles(page)).containsExactly("Exolon", "tetris");
        assertThat(page.items().getNumber()).isEqualTo(1);
        assertThat(page.items().getTotalPages()).isEqualTo(2);
        assertThat(page.items().getTotalElements()).isEqualTo(4);
    }

    @Test
    void countsTheValuesOfEveryFilterOverTheWholeResultNotThePage() {
        SoftwareCatalogPage page = catalog.find(vector(), PageRequest.of(0, 1));

        assertThat(page.facets().years()).containsExactlyInAnyOrder(new YearCount(1987, 2), new YearCount(1989, 1));
        assertThat(page.facets().categories())
                .containsExactlyInAnyOrder(
                        new IdCount(GAMES, 3),
                        new IdCount(ACTION, 2),
                        new IdCount(SHOOTERS, 1),
                        new IdCount(PUZZLE, 1),
                        new IdCount(DEMOSCENE, 1));
        assertThat(page.facets().countries())
                .containsExactlyInAnyOrder(new IdCount(FINLAND, 2), new IdCount(BRITAIN, 1));
    }

    @Test
    void countsTheValuesOfAChosenFilterWithoutItsOwnConditionAndTheOthersWithIt() {
        SoftwareCatalogPage page = find(new SoftwareCatalogFilter(null, "vector", 1987, null, null));

        assertThat(page.facets().years()).containsExactlyInAnyOrder(new YearCount(1987, 2), new YearCount(1989, 1));
        assertThat(page.facets().categories())
                .containsExactlyInAnyOrder(new IdCount(GAMES, 2), new IdCount(ACTION, 2), new IdCount(SHOOTERS, 1));
        assertThat(page.facets().countries())
                .containsExactlyInAnyOrder(new IdCount(FINLAND, 1), new IdCount(BRITAIN, 1));
    }

    @Test
    void theValuesOfAChosenFilterKeepTheKeywordTheOtherFiltersAndThePlatform() {
        SoftwareCatalogPage page = find(new SoftwareCatalogFilter("e", "vector", 1987, null, FINLAND));

        assertThat(titles(page)).containsExactly("Elite");
        assertThat(page.facets().years()).containsExactly(new YearCount(1987, 1));
        assertThat(page.facets().countries())
                .containsExactlyInAnyOrder(new IdCount(FINLAND, 1), new IdCount(BRITAIN, 1));
    }

    @Test
    void failsWhileTypesenseIsDown() {
        TypesenseProbe probe = new TypesenseProbe(typesense);
        probe.freeze();

        assertThatThrownBy(() -> find(vector())).isInstanceOf(RuntimeException.class);
    }
}
