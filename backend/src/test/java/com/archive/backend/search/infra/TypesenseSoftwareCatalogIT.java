package com.archive.backend.search.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.IntegrationTest;
import com.archive.backend.search.TypesenseProbe;
import com.archive.backend.search.domain.SearchCollection;
import com.archive.backend.search.domain.SearchEntityType;
import com.archive.backend.search.domain.SoftwareDocument;
import com.archive.backend.search.read.CodeCount;
import com.archive.backend.search.read.IdCount;
import com.archive.backend.search.read.SoftwareCatalogFilter;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.search.read.SoftwareCatalogPage;
import com.archive.backend.search.read.YearCount;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.typesense.TypesenseContainer;

/**
 * The software catalog read from Typesense (docs/domain/search.md#software-catalog): every test puts a collection of its
 * own in use, holding exactly the software below.
 */
@IntegrationTest
class TypesenseSoftwareCatalogIT {

    private static final UUID GAMES = UUID.fromString("00000000-0000-0000-00fd-0000000000c1");
    private static final UUID ACTION = UUID.fromString("00000000-0000-0000-00fd-0000000000c2");
    private static final UUID SHOOTERS = UUID.fromString("00000000-0000-0000-00fd-0000000000c3");
    private static final UUID PUZZLE = UUID.fromString("00000000-0000-0000-00fd-0000000000c4");
    private static final UUID DEMOSCENE = UUID.fromString("00000000-0000-0000-00fd-0000000000c5");

    private static final UUID FINLAND = UUID.fromString("00000000-0000-0000-00fd-0000000000f1");
    private static final UUID BRITAIN = UUID.fromString("00000000-0000-0000-00fd-0000000000f2");

    private static final UUID HELSINKI = UUID.fromString("00000000-0000-0000-00fd-0000000000e1");
    private static final UUID ESPOO = UUID.fromString("00000000-0000-0000-00fd-0000000000e2");
    private static final UUID LONDON = UUID.fromString("00000000-0000-0000-00fd-0000000000e3");

    private static final UUID TITLE_SCREEN = UUID.fromString("00000000-0000-0000-00fd-0000000000a1");
    private static final UUID IN_GAME = UUID.fromString("00000000-0000-0000-00fd-0000000000a2");

    private static final SoftwareDocument ELITE = located(
            new SoftwareDocument(
                    UUID.fromString("00000000-0000-0000-00fd-000000000001"),
                    "Elite",
                    "1987-05-01",
                    List.of("Элита"),
                    "vector",
                    1987,
                    List.of(SHOOTERS, ACTION, GAMES),
                    List.of(FINLAND),
                    List.of(TITLE_SCREEN, IN_GAME),
                    List.of("Ultimate", "Zed")),
            List.of(HELSINKI),
            Map.of("computers", List.of("vector06c"), "sound", List.of("ay8910")));
    private static final SoftwareDocument EXOLON = located(
            software(
                    2,
                    "Exolon",
                    List.of(),
                    "vector",
                    LocalDate.of(1987, 9, 1),
                    List.of(ACTION, GAMES),
                    List.of(BRITAIN)),
            List.of(LONDON),
            Map.of("computers", List.of("krista2")));
    private static final SoftwareDocument TETRIS = located(
            software(
                    3,
                    "tetris",
                    List.of(),
                    "vector",
                    LocalDate.of(1989, 1, 1),
                    List.of(PUZZLE, GAMES),
                    List.of(FINLAND)),
            List.of(ESPOO),
            Map.of("computers", List.of("vector06c")));
    private static final SoftwareDocument DEMO =
            software(4, "Demo", List.of(), "vector", null, List.of(DEMOSCENE), List.of());
    private static final SoftwareDocument BOLDER = located(
            software(
                    5,
                    "Bolder Dash",
                    List.of(),
                    "bk",
                    LocalDate.of(1987, 1, 1),
                    List.of(PUZZLE, GAMES),
                    List.of(FINLAND)),
            List.of(HELSINKI),
            Map.of("computers", List.of("bk0011m"), "sound", List.of("ay8910")));

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
                releaseDate == null ? null : releaseDate.toString(),
                alternativeTitles,
                platform,
                releaseDate == null ? null : releaseDate.getYear(),
                categories,
                countries);
    }

    /** The software with the cities it comes from and its hardware by group, its releases' among it. */
    private static SoftwareDocument located(
            SoftwareDocument document, List<UUID> cities, Map<String, List<String>> hardware) {
        return new SoftwareDocument(
                document.id(),
                document.title(),
                document.releaseDate(),
                document.alternativeTitles(),
                document.platform(),
                document.year(),
                document.categories(),
                document.countries(),
                document.screenshots(),
                document.madeBy(),
                cities,
                hardware);
    }

    private static SoftwareCatalogFilter filter(
            @Nullable String platform,
            @Nullable UUID country,
            @Nullable UUID city,
            List<String> platforms,
            Map<String, List<String>> hardware) {
        return new SoftwareCatalogFilter(null, platform, null, null, country, city, platforms, hardware);
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
                .containsExactly(new SoftwareCatalogItem(
                        ELITE.id(), "Elite", "1987-05-01", List.of(TITLE_SCREEN, IN_GAME), List.of("Ultimate", "Zed")));
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
    void narrowsByTheCityWithinItsCountry() {
        assertThat(titles(find(filter("vector", FINLAND, HELSINKI, List.of(), Map.of()))))
                .containsExactly("Elite");
    }

    @Test
    void narrowsByAnyOfThePlatformsChosen() {
        assertThat(titles(find(filter(null, null, null, List.of("bk", "vector"), Map.of()))))
                .containsExactly("Bolder Dash", "Demo", "Elite", "Exolon", "tetris");
        assertThat(titles(find(filter(null, null, null, List.of("bk"), Map.of()))))
                .containsExactly("Bolder Dash");
    }

    @Test
    void findsSoftwareWithAnyHardwareChosenInAGroup() {
        assertThat(titles(find(
                        filter("vector", null, null, List.of(), Map.of("computers", List.of("vector06c", "krista2"))))))
                .containsExactly("Elite", "Exolon", "tetris");
    }

    @Test
    void findsSoftwareWithHardwareOfEveryGroupChosen() {
        assertThat(titles(find(filter(
                        "vector",
                        null,
                        null,
                        List.of(),
                        Map.of("computers", List.of("vector06c", "krista2"), "sound", List.of("ay8910"))))))
                .containsExactly("Elite");
    }

    @Test
    void countsTheCitiesThePlatformsAndTheHardwareOverTheWholeResult() {
        SoftwareCatalogPage page = catalog.find(filter(null, null, null, List.of(), Map.of()), PageRequest.of(0, 1));

        assertThat(page.facets().cities())
                .containsExactlyInAnyOrder(new IdCount(HELSINKI, 2), new IdCount(ESPOO, 1), new IdCount(LONDON, 1));
        assertThat(page.facets().platforms())
                .containsExactlyInAnyOrder(new CodeCount("vector", 4), new CodeCount("bk", 1));
        assertThat(page.facets().hardware())
                .containsExactlyInAnyOrder(
                        new CodeCount("vector06c", 2),
                        new CodeCount("krista2", 1),
                        new CodeCount("bk0011m", 1),
                        new CodeCount("ay8910", 2));
    }

    @Test
    void countsTheCitiesOfAChosenCityWithoutItButWithinTheCountry() {
        SoftwareCatalogPage page = find(filter("vector", FINLAND, HELSINKI, List.of(), Map.of()));

        assertThat(page.facets().cities()).containsExactlyInAnyOrder(new IdCount(HELSINKI, 1), new IdCount(ESPOO, 1));
    }

    @Test
    void countsTheChosenPlatformsWithoutTheirOwnCondition() {
        SoftwareCatalogPage page = find(filter(null, null, null, List.of("bk"), Map.of()));

        assertThat(page.facets().platforms())
                .containsExactlyInAnyOrder(new CodeCount("vector", 4), new CodeCount("bk", 1));
    }

    @Test
    void countsAChosenHardwareGroupWithoutItsOwnConditionAndTheOtherGroupsWithIt() {
        SoftwareCatalogPage page =
                find(filter("vector", null, null, List.of(), Map.of("computers", List.of("vector06c"))));

        assertThat(titles(page)).containsExactly("Elite", "tetris");
        assertThat(page.facets().hardware())
                .containsExactlyInAnyOrder(
                        new CodeCount("vector06c", 2), new CodeCount("krista2", 1), new CodeCount("ay8910", 1));
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
        assertThat(page.facets().countries())
                .containsExactlyInAnyOrder(new IdCount(FINLAND, 2), new IdCount(BRITAIN, 1));
    }

    @Test
    void countsTheValuesOfAChosenFilterWithoutItsOwnConditionAndTheOthersWithIt() {
        SoftwareCatalogPage page = find(new SoftwareCatalogFilter(null, "vector", 1987, null, null));

        assertThat(page.facets().years()).containsExactlyInAnyOrder(new YearCount(1987, 2), new YearCount(1989, 1));
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
    void countsTheFiltersInsideTheCategory() {
        SoftwareCatalogPage page = find(new SoftwareCatalogFilter(null, "vector", null, PUZZLE, null));

        assertThat(titles(page)).containsExactly("tetris");
        assertThat(page.facets().years()).containsExactly(new YearCount(1989, 1));
        assertThat(page.facets().countries()).containsExactly(new IdCount(FINLAND, 1));
    }

    @Test
    void failsWhileTypesenseIsDown() {
        TypesenseProbe probe = new TypesenseProbe(typesense);
        probe.freeze();

        assertThatThrownBy(() -> find(vector())).isInstanceOf(RuntimeException.class);
    }
}
