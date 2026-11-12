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
import com.archive.backend.search.read.FoundAuthor;
import com.archive.backend.search.read.FoundGroup;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.typesense.TypesenseContainer;

/**
 * The full search of people and of groups (docs/domain/search.md#full-search): every test puts collections of its own
 * in use, holding exactly the entities below.
 */
@SpringBootTest
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class})
class TypesenseFullSearchIT {

    private static final UUID ZED = UUID.fromString("00000000-0000-0000-00fb-000000000011");
    private static final UUID ZORRO = UUID.fromString("00000000-0000-0000-00fb-000000000012");
    private static final UUID JOHN = UUID.fromString("00000000-0000-0000-00fb-000000000013");
    private static final UUID ZAK = UUID.fromString("00000000-0000-0000-00fb-000000000014");
    private static final UUID SQUAD = UUID.fromString("00000000-0000-0000-00fb-000000000021");
    private static final UUID ZONE = UUID.fromString("00000000-0000-0000-00fb-000000000022");
    private static final UUID CREW = UUID.fromString("00000000-0000-0000-00fb-000000000023");

    /** Three people of the vector matched by "z", one of them by the real name only; one of the BK. */
    private static final List<AuthorDocument> AUTHORS = List.of(
            new AuthorDocument(ZED, "Zed", List.of("Zed"), "Zed Smith", List.of("vector")),
            new AuthorDocument(ZORRO, "Zorro", List.of("Zorro"), null, List.of("vector")),
            new AuthorDocument(JOHN, "John", List.of("John"), "Zak Doe", List.of("vector")),
            new AuthorDocument(ZAK, "Zak", List.of("Zak"), null, List.of("bk")));

    private static final List<GroupDocument> GROUPS = List.of(
            new GroupDocument(SQUAD, "ZS", List.of("ZS"), "Zed Squad", List.of("vector")),
            new GroupDocument(ZONE, "Zone", List.of("Zone"), "Zone", List.of("vector")),
            new GroupDocument(CREW, "CRW", List.of("CRW"), "Zero Crew", List.of("bk")));

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
    }

    @AfterEach
    void resume() {
        new TypesenseProbe(typesense).resume();
    }

    @Test
    void givesThePeopleAPageAtATimeWithTheCountOfAll() {
        Page<FoundAuthor> first = search.authors("z", "vector", PageRequest.of(0, 2));
        Page<FoundAuthor> second = search.authors("z", "vector", PageRequest.of(1, 2));

        assertThat(first.getContent()).hasSize(2);
        assertThat(first.getTotalElements()).isEqualTo(3);
        assertThat(first.getTotalPages()).isEqualTo(2);
        assertThat(Stream.concat(first.stream(), second.stream()).map(FoundAuthor::id))
                .containsExactlyInAnyOrder(ZED, ZORRO, JOHN);
    }

    @Test
    void givesAPersonUnderTheMainNameWithTheRealName() {
        assertThat(search.authors("smith", null, PageRequest.of(0, 50)).getContent())
                .containsExactly(new FoundAuthor(ZED, "Zed", "Zed Smith"));
    }

    @Test
    void peopleFollowThePlatform() {
        assertThat(search.authors("zak", "bk", PageRequest.of(0, 50)).map(FoundAuthor::id))
                .containsExactly(ZAK);
        assertThat(search.authors("zak", null, PageRequest.of(0, 50)).map(FoundAuthor::id))
                .containsExactlyInAnyOrder(ZAK, JOHN);
    }

    @Test
    void givesTheGroupsAPageAtATimeOfThePlatform() {
        Page<FoundGroup> first = search.groups("z", "vector", PageRequest.of(0, 1));
        Page<FoundGroup> second = search.groups("z", "vector", PageRequest.of(1, 1));

        assertThat(first.getTotalElements()).isEqualTo(2);
        assertThat(Stream.concat(first.stream(), second.stream()))
                .containsExactlyInAnyOrder(new FoundGroup(SQUAD, "ZS"), new FoundGroup(ZONE, "Zone"));
    }

    @Test
    void aPageBeyondTheMatchesIsEmpty() {
        Page<FoundGroup> page = search.groups("zero", null, PageRequest.of(3, 50));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void failsWhileTypesenseIsDown() {
        new TypesenseProbe(typesense).freeze();

        assertThatThrownBy(() -> search.authors("z", null, PageRequest.of(0, 50)))
                .isInstanceOf(RuntimeException.class);
    }
}
