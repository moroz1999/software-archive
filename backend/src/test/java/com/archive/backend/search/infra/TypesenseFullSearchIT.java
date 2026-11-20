package com.archive.backend.search.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.IntegrationTest;
import com.archive.backend.search.TypesenseProbe;
import com.archive.backend.search.domain.AuthorDocument;
import com.archive.backend.search.domain.GroupDocument;
import com.archive.backend.search.domain.SearchCollection;
import com.archive.backend.search.domain.SearchDocument;
import com.archive.backend.search.domain.SearchEntityType;
import com.archive.backend.search.domain.SoftwareDocument;
import com.archive.backend.search.read.FoundAuthor;
import com.archive.backend.search.read.FoundGroup;
import com.archive.backend.search.read.FoundSet;
import com.archive.backend.search.read.FoundType;
import com.archive.backend.search.read.FullSearchResult;
import com.archive.backend.search.read.SoftwareCatalogItem;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.typesense.TypesenseContainer;

/**
 * The full search: one list of people, then groups, then software, cut into pages (docs/domain/search.md#full-search).
 * Every test puts collections of its own in use, holding exactly the entities below: matched by "z", three people
 * and two groups of the vector, four software of the vector, and one of each of the BK.
 */
@IntegrationTest
class TypesenseFullSearchIT {

    private static final Set<FoundType> ALL = EnumSet.allOf(FoundType.class);

    private static UUID id(int n) {
        return UUID.fromString("00000000-0000-0000-00fb-%012d".formatted(n));
    }

    private static final List<AuthorDocument> AUTHORS = List.of(
            AuthorDocument.person(id(11), "Zed", List.of("Zed"), "Zed Smith", List.of("vector")),
            AuthorDocument.person(id(12), "Zorro", List.of("Zorro"), null, List.of("vector")),
            AuthorDocument.person(id(13), "John", List.of("John"), "Zak Doe", List.of("vector")),
            AuthorDocument.person(id(14), "Zak", List.of("Zak"), null, List.of("bk")),
            AuthorDocument.label(id(15), "Phantom", List.of("msx")));

    private static final List<GroupDocument> GROUPS = List.of(
            new GroupDocument(id(21), "ZS", List.of("ZS"), "Zed Squad", List.of("vector")),
            new GroupDocument(id(22), "Zone", List.of("Zone"), "Zone", List.of("vector")),
            new GroupDocument(id(23), "CRW", List.of("CRW"), "Zero Crew", List.of("bk")));

    private static final List<SoftwareDocument> SOFTWARE = IntStream.rangeClosed(31, 35)
            .mapToObj(n -> new SoftwareDocument(
                    id(n),
                    "Zynaps " + n,
                    "1987-05-01",
                    List.of(),
                    n == 35 ? "bk" : "vector",
                    1987,
                    List.of(),
                    List.of()))
            .toList();

    @Autowired
    private TypesenseFullSearch search;

    @Autowired
    private TypesenseSearchIndex index;

    @Autowired
    private TypesenseContainer typesense;

    private void putInUse(SearchEntityType type, List<? extends SearchDocument> documents) {
        SearchCollection collection = index.create(type);
        index.load(collection, documents);
        index.switchTo(collection);
    }

    @BeforeEach
    void putTheEntitiesInUse() {
        putInUse(SearchEntityType.AUTHOR, AUTHORS);
        putInUse(SearchEntityType.GROUP, GROUPS);
        putInUse(SearchEntityType.SOFTWARE, SOFTWARE);
    }

    @AfterEach
    void resume() {
        new TypesenseProbe(typesense).resume();
    }

    private static List<String> kindsAndSizes(FullSearchResult result) {
        return result.sets().stream()
                .map(set -> set.kind() + ":" + size(set) + "/" + set.total())
                .toList();
    }

    private static int size(FoundSet set) {
        return switch (set) {
            case FoundSet.Authors authors -> authors.items().size();
            case FoundSet.Groups groups -> groups.items().size();
            case FoundSet.Software software -> software.items().size();
        };
    }

    @Test
    void listsPeopleThenGroupsThenSoftwareAcrossThePages() {
        FullSearchResult first = search.find("z", "vector", ALL, 0, 4);
        FullSearchResult second = search.find("z", "vector", ALL, 1, 4);
        FullSearchResult third = search.find("z", "vector", ALL, 2, 4);

        assertThat(first.total()).isEqualTo(9);
        assertThat(first.totalPages()).isEqualTo(3);
        assertThat(kindsAndSizes(first)).containsExactly("AUTHOR:3/3", "GROUP:1/2");
        assertThat(kindsAndSizes(second)).containsExactly("GROUP:1/2", "SOFTWARE:3/4");
        assertThat(kindsAndSizes(third)).containsExactly("SOFTWARE:1/4");
    }

    @Test
    void givesEveryMatchOnceOverThePages() {
        List<UUID> groups = IntStream.range(0, 3)
                .mapToObj(page -> search.find("z", "vector", ALL, page, 4))
                .flatMap(result -> result.sets().stream())
                .flatMap(set -> set instanceof FoundSet.Groups g ? g.items().stream() : Stream.empty())
                .map(FoundGroup::id)
                .toList();

        assertThat(groups).containsExactlyInAnyOrder(id(21), id(22));
    }

    @Test
    void givesThePeopleUnderTheMainNameAndTheSoftwareAsCards() {
        FullSearchResult result = search.find("smith", null, ALL, 0, 50);
        FullSearchResult software = search.find("zynaps", "bk", ALL, 0, 50);

        assertThat(result.sets())
                .containsExactly(
                        new FoundSet.Authors(1, List.of(new FoundAuthor(id(11), "Zed", "Zed Smith", "person"))));
        assertThat(software.sets())
                .containsExactly(
                        new FoundSet.Software(1, List.of(new SoftwareCatalogItem(id(35), "Zynaps 35", "1987-05-01"))));
    }

    @Test
    void findsALabelOfNobodyAmongThePeople() {
        assertThat(search.find("phantom", null, ALL, 0, 50).sets())
                .containsExactly(new FoundSet.Authors(1, List.of(new FoundAuthor(id(15), "Phantom", null, "label"))));
    }

    @Test
    void searchesOnlyTheKindsAsked() {
        FullSearchResult result = search.find("z", "vector", Set.of(FoundType.SOFTWARE, FoundType.GROUP), 0, 4);

        assertThat(result.total()).isEqualTo(6);
        assertThat(kindsAndSizes(result)).containsExactly("GROUP:2/2", "SOFTWARE:2/4");
    }

    @Test
    void aPageBeyondTheMatchesHasNoSets() {
        FullSearchResult result = search.find("z", "vector", ALL, 5, 4);

        assertThat(result.sets()).isEmpty();
        assertThat(result.total()).isEqualTo(9);
    }

    @Test
    void findsNothingOfAKeywordMatchingNothing() {
        assertThat(search.find("qqqq", null, ALL, 0, 50)).isEqualTo(FullSearchResult.empty(0, 50));
    }

    @Test
    void failsWhileTypesenseIsDown() {
        new TypesenseProbe(typesense).freeze();

        assertThatThrownBy(() -> search.find("z", null, ALL, 0, 50)).isInstanceOf(RuntimeException.class);
    }
}
