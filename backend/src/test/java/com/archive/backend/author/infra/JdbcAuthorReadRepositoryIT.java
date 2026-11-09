package com.archive.backend.author.infra;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.author.read.AuthorAlias;
import com.archive.backend.author.read.AuthorCatalogEntry;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.author.read.AuthorGroup;
import com.archive.backend.author.read.AuthorLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcAuthorReadRepository.class})
class JdbcAuthorReadRepositoryIT {

    private static final PageRequest FIRST_PAGE = PageRequest.of(0, 100);

    private static final UUID FINLAND = id(1);
    private static final UUID NORWAY = id(2);
    private static final UUID HELSINKI = id(11);
    private static final UUID TAMPERE = id(12);

    /** Aliases Zed (display name) and Axe; located in Helsinki (primary) and Norway; member of Crew. */
    private static final UUID ZED = id(101);
    /** Aliases Mona and Bob, none marked: Bob is the main name; located in Norway. */
    private static final UUID BOB = id(102);
    /** No aliases, only a real name; located in Tampere. */
    private static final UUID CARL = id(103);
    /** No name at all: not an author. */
    private static final UUID NAMELESS = id(104);

    private static final UUID ZED_ALIAS = id(201);
    private static final UUID AXE_ALIAS = id(202);
    private static final UUID CREW = id(301);

    @Autowired
    private JdbcAuthorReadRepository reads;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void insert() {
        jdbc.update("INSERT INTO country (id, name) VALUES (?, 'Finland'), (?, 'Norway')", FINLAND, NORWAY);
        jdbc.update("INSERT INTO city (id, name, country_id) VALUES (?, 'Helsinki', ?), (?, 'Tampere', ?)", HELSINKI, FINLAND, TAMPERE, FINLAND);

        jdbc.update("INSERT INTO person (id, real_name, notes) VALUES (?, 'John Doe', 'Coder since 1984'), (?, NULL, NULL), (?, 'Carl Real', NULL), (?, NULL, NULL)",
                ZED, BOB, CARL, NAMELESS);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew')", CREW);
        jdbc.update("""
                INSERT INTO alias (id, name, person_id, group_id, is_display_name) VALUES
                    (?, 'Zed', ?, NULL, TRUE), (?, 'Axe', ?, NULL, FALSE),
                    (gen_random_uuid(), 'Mona', ?, NULL, FALSE), (gen_random_uuid(), 'Bob', ?, NULL, FALSE),
                    (gen_random_uuid(), 'Crew Alias', NULL, ?, TRUE)
                """, ZED_ALIAS, ZED, AXE_ALIAS, ZED, BOB, BOB, CREW);
        jdbc.update("INSERT INTO group_member (group_id, alias_id) VALUES (?, ?)", CREW, AXE_ALIAS);
        jdbc.update("""
                INSERT INTO entity_location (entity_type, entity_id, city_id, country_id, is_primary) VALUES
                    ('PERSON', ?, NULL, ?, FALSE), ('PERSON', ?, ?, NULL, TRUE),
                    ('PERSON', ?, NULL, ?, TRUE),
                    ('PERSON', ?, ?, ?, TRUE)
                """, ZED, NORWAY, ZED, HELSINKI, BOB, NORWAY, CARL, TAMPERE, FINLAND);
    }

    @Test
    void listsPeopleUnderTheirMainNameAndTheirOtherAliasesByName() {
        Page<AuthorCatalogEntry> page = reads.findCatalogPage(null, null, null, FIRST_PAGE);

        assertThat(page.getContent()).containsExactly(
                new AuthorCatalogEntry(ZED, "Axe", "John Doe", "Zed"),
                new AuthorCatalogEntry(BOB, "Bob", null, null),
                new AuthorCatalogEntry(CARL, "Carl Real", null, null),
                new AuthorCatalogEntry(BOB, "Mona", null, "Bob"),
                new AuthorCatalogEntry(ZED, "Zed", "John Doe", null));
        assertThat(page.getTotalElements()).isEqualTo(5);
    }

    @Test
    void namesTheRealNameNextToEveryEntryOfAPersonWhoHasOneButNotNextToItself() {
        Page<AuthorCatalogEntry> page = reads.findCatalogPage(null, null, null, FIRST_PAGE);

        assertThat(page.getContent()).extracting(AuthorCatalogEntry::name, AuthorCatalogEntry::realName).containsExactly(
                tuple("Axe", "John Doe"), tuple("Bob", null), tuple("Carl Real", null), tuple("Mona", null), tuple("Zed", "John Doe"));
    }

    @Test
    void readsTheRequestedPage() {
        Page<AuthorCatalogEntry> page = reads.findCatalogPage(null, null, null, PageRequest.of(1, 2));

        assertThat(page.getContent()).extracting(AuthorCatalogEntry::name).containsExactly("Carl Real", "Mona");
        assertThat(page.getTotalElements()).isEqualTo(5);
    }

    @Test
    void filtersByTheFirstLetterInAnyCase() {
        jdbc.update("INSERT INTO alias (name, person_id) VALUES ('mike', ?)", CARL);

        assertThat(reads.findCatalogPage("M", null, null, FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name).containsExactly("mike", "Mona");
    }

    @Test
    void filtersByACyrillicLetterCountingYoAsYe() {
        jdbc.update("INSERT INTO alias (name, person_id) VALUES ('Ёжик', ?), ('Енот', ?), ('Борис', ?)", CARL, CARL, CARL);

        assertThat(reads.findCatalogPage("Е", null, null, FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name).containsExactlyInAnyOrder("Ёжик", "Енот");
    }

    @Test
    void filtersNamesStartingWithAnythingElseByTheRestSign() {
        jdbc.update("INSERT INTO alias (name, person_id) VALUES ('8bit', ?), ('Ärger', ?), ('_under', ?), ('Борис', ?)", CARL, CARL, CARL, CARL);

        assertThat(reads.findCatalogPage("#", null, null, FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name).containsExactlyInAnyOrder("8bit", "Ärger", "_under");
    }

    @Test
    void filtersByACountryOfAnyLocationGivenDirectlyOrThroughACity() {
        assertThat(reads.findCatalogPage(null, FINLAND, null, FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name).containsExactly("Axe", "Carl Real", "Zed");
        assertThat(reads.findCatalogPage(null, NORWAY, null, FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name).containsExactly("Axe", "Bob", "Mona", "Zed");
    }

    @Test
    void filtersByACity() {
        assertThat(reads.findCatalogPage(null, null, TAMPERE, FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name).containsExactly("Carl Real");
    }

    @Test
    void combinesTheFilters() {
        assertThat(reads.findCatalogPage("Z", FINLAND, HELSINKI, FIRST_PAGE).getContent())
                .containsExactly(new AuthorCatalogEntry(ZED, "Zed", "John Doe", null));
    }

    @Test
    void readsTheDetailsOfAnAuthor() {
        assertThat(reads.findById(ZED)).contains(new AuthorDetails(
                ZED, "Zed", "John Doe", "Coder since 1984",
                List.of(new AuthorAlias(AXE_ALIAS, "Axe"), new AuthorAlias(ZED_ALIAS, "Zed")),
                List.of(new AuthorLocation("Finland", "Helsinki", true), new AuthorLocation("Norway", null, false)),
                List.of(new AuthorGroup(CREW, "Crew"))));
    }

    @Test
    void readsAnAuthorKnownOnlyByTheRealName() {
        assertThat(reads.findById(CARL)).contains(new AuthorDetails(
                CARL, "Carl Real", "Carl Real", null, List.of(), List.of(new AuthorLocation("Finland", "Tampere", true)), List.of()));
    }

    @Test
    void findsNothingForANamelessPerson() {
        assertThat(reads.findById(NAMELESS)).isEmpty();
    }

    @Test
    void findsNothingForAnUnknownId() {
        assertThat(reads.findById(UUID.randomUUID())).isEmpty();
    }

    private static UUID id(int n) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n));
    }
}
