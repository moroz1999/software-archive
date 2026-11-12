package com.archive.backend.search.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.archive.backend.search.TypesenseProbe;
import com.archive.backend.search.domain.AuthorDocument;
import com.archive.backend.search.domain.GroupDocument;
import com.archive.backend.search.domain.SearchCollection;
import com.archive.backend.search.domain.SearchDocument;
import com.archive.backend.search.domain.SearchEntityType;
import com.archive.backend.search.domain.SoftwareDocument;
import com.archive.backend.search.read.QuickAuthor;
import com.archive.backend.search.read.QuickGroup;
import com.archive.backend.search.read.QuickSearchResult;
import com.archive.backend.search.read.SoftwareCatalogItem;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.typesense.TypesenseContainer;

/**
 * The quick search over the collections of software, people and groups (docs/domain/search.md#quick-search): every
 * test puts collections of its own in use, holding exactly the entities below.
 */
@SpringBootTest
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class})
class TypesenseQuickSearchIT {

    private static final SoftwareDocument ELITE = software(1, "Elite", List.of("Элита"), "vector");
    /** With Elite, seven titles of the vector beginning with E; and one of the BK. */
    private static final List<SoftwareDocument> E_TITLES = IntStream.rangeClosed(2, 7)
            .mapToObj(n -> software(n, "Exolon " + n, List.of(), "vector"))
            .toList();

    private static final SoftwareDocument EXOLON_BK = software(8, "Exolon BK", List.of(), "bk");

    private static final UUID ZED = UUID.fromString("00000000-0000-0000-00fc-000000000011");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-00fc-000000000012");
    private static final UUID CARL = UUID.fromString("00000000-0000-0000-00fc-000000000013");
    private static final UUID SQUAD = UUID.fromString("00000000-0000-0000-00fc-000000000021");
    private static final UUID CREW = UUID.fromString("00000000-0000-0000-00fc-000000000022");

    private static final AuthorDocument ZED_DOC =
            new AuthorDocument(ZED, "Zed", List.of("Zed", "Zx"), "John Doe", List.of("vector"));
    private static final AuthorDocument BOB_DOC =
            new AuthorDocument(BOB, "Bob", List.of("Bob"), "Zedd Smith", List.of("bk"));
    /** Known by the real name alone: it is the main name too. */
    private static final AuthorDocument CARL_DOC =
            new AuthorDocument(CARL, "Carl Real", List.of(), "Carl Real", List.of("vector"));

    private static final GroupDocument SQUAD_DOC =
            new GroupDocument(SQUAD, "ZS", List.of("ZS", "Zeta Squad"), "Zed Squad", List.of("vector"));
    private static final GroupDocument CREW_DOC =
            new GroupDocument(CREW, "CRW", List.of("CRW"), "Zero Crew", List.of("bk"));

    @Autowired
    private TypesenseQuickSearch search;

    @Autowired
    private TypesenseSearchIndex index;

    @Autowired
    private TypesenseContainer typesense;

    private static SoftwareDocument software(int n, String title, List<String> alternativeTitles, String platform) {
        return new SoftwareDocument(
                UUID.fromString("00000000-0000-0000-00fc-00000000000" + n),
                title,
                LocalDate.of(1987, 5, 1),
                alternativeTitles,
                platform,
                1987,
                List.of(),
                List.of());
    }

    private void putInUse(SearchEntityType type, List<? extends SearchDocument> documents) {
        SearchCollection collection = index.create(type);
        index.load(collection, documents);
        index.switchTo(collection);
    }

    @BeforeEach
    void putTheEntitiesInUse() {
        putInUse(
                SearchEntityType.SOFTWARE,
                java.util.stream.Stream.concat(java.util.stream.Stream.of(ELITE, EXOLON_BK), E_TITLES.stream())
                        .toList());
        putInUse(SearchEntityType.AUTHOR, List.of(ZED_DOC, BOB_DOC, CARL_DOC));
        putInUse(SearchEntityType.GROUP, List.of(SQUAD_DOC, CREW_DOC));
    }

    @AfterEach
    void resume() {
        new TypesenseProbe(typesense).resume();
    }

    private QuickSearchResult find(String keyword, @Nullable String platform) {
        return search.find(keyword, platform, 5);
    }

    @Test
    void findsSoftwareByTheBeginningOfItsTitleOrOfAnAlternativeTitle() {
        assertThat(find("Элит", null).software().hits())
                .containsExactly(new SoftwareCatalogItem(ELITE.id(), "Elite", LocalDate.of(1987, 5, 1)));
        assertThat(find("eli", null).software().hits())
                .extracting(SoftwareCatalogItem::title)
                .containsExactly("Elite");
    }

    @Test
    void givesAtMostTheLimitOfMatchesAndTheCountOfAllOfThem() {
        QuickSearchResult result = find("e", "vector");

        assertThat(result.software().hits()).hasSize(5);
        assertThat(result.software().total()).isEqualTo(7);
    }

    @Test
    void findsAPersonByAnyLabelOrTheRealNameUnderTheMainName() {
        QuickAuthor zed = new QuickAuthor(ZED, "Zed", "John Doe");

        assertThat(find("zx", null).authors().hits()).containsExactly(zed);
        assertThat(find("john", null).authors().hits()).containsExactly(zed);
    }

    @Test
    void givesNoRealNameForAPersonShownUnderIt() {
        assertThat(find("carl", null).authors().hits()).containsExactly(new QuickAuthor(CARL, "Carl Real", null));
    }

    @Test
    void findsAGroupByAnyLabelOrItsNameUnderTheMainName() {
        assertThat(find("zeta", null).groups().hits()).containsExactly(new QuickGroup(SQUAD, "ZS"));
        assertThat(find("zero", null).groups().hits()).containsExactly(new QuickGroup(CREW, "CRW"));
    }

    @Test
    void everySectionFollowsThePlatform() {
        QuickSearchResult vector = find("z", "vector");
        QuickSearchResult bk = find("z", "bk");

        assertThat(vector.authors().hits()).extracting(QuickAuthor::id).containsExactly(ZED);
        assertThat(vector.groups().hits()).extracting(QuickGroup::id).containsExactly(SQUAD);
        assertThat(bk.authors().hits()).extracting(QuickAuthor::id).containsExactly(BOB);
        assertThat(bk.groups().hits()).extracting(QuickGroup::id).containsExactly(CREW);
        assertThat(find("exolon", "bk").software().hits())
                .extracting(SoftwareCatalogItem::title)
                .containsExactly("Exolon BK");
    }

    @Test
    void aSectionWithoutMatchesIsEmpty() {
        QuickSearchResult result = find("elite", null);

        assertThat(result.authors().hits()).isEmpty();
        assertThat(result.authors().total()).isZero();
        assertThat(result.groups().total()).isZero();
    }

    @Test
    void failsWhileTypesenseIsDown() {
        new TypesenseProbe(typesense).freeze();

        assertThatThrownBy(() -> find("elite", null)).isInstanceOf(RuntimeException.class);
    }
}
