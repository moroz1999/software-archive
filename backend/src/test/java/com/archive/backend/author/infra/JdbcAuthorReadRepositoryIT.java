package com.archive.backend.author.infra;

import static com.archive.backend.geo.GeoTestRows.city;
import static com.archive.backend.geo.GeoTestRows.country;
import static com.archive.backend.user.TestActors.ADDED;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.author.read.AuthorCatalogEntry;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.author.read.AuthorGroup;
import com.archive.backend.author.read.AuthorLabel;
import com.archive.backend.author.read.AuthorLocation;
import com.archive.backend.user.TestActors;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcAuthorReadRepository.class})
class JdbcAuthorReadRepositoryIT {

    private static final PageRequest FIRST_PAGE = PageRequest.of(0, 100);

    private static final UUID FINLAND = id(1);
    private static final UUID NORWAY = id(2);
    private static final UUID HELSINKI = id(11);
    private static final UUID TAMPERE = id(12);

    /** Labels Zed (display name) and Axe; located in Helsinki (primary) and Norway; member of Crew as Axe and as Zed. */
    private static final UUID ZED = id(101);
    /** No real name; labels Mona and Bob, none marked: Bob is the main name; located in Norway. */
    private static final UUID BOB = id(102);
    /** No labels, only a real name; located in Tampere. */
    private static final UUID CARL = id(103);
    /** No name at all: not an author. */
    private static final UUID NAMELESS = id(104);

    private static final UUID ZED_LABEL = id(201);
    private static final UUID AXE_LABEL = id(202);
    private static final UUID GHOST = id(203);
    private static final UUID SHADOW = id(204);
    private static final UUID MONA_LABEL = id(205);
    private static final UUID BOB_LABEL = id(206);
    /** Known by its label Crew Label; Zed is a member of it under both labels. */
    private static final UUID CREW = id(301);
    /** Known only by its name; Zed is a member of it under the main name. */
    private static final UUID BAND = id(302);

    @Autowired
    private JdbcAuthorReadRepository reads;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void insert() {
        country(jdbc, FINLAND, "Финляндия", "Finland");
        country(jdbc, NORWAY, "Норвегия", "Norway");
        city(jdbc, HELSINKI, FINLAND, "Хельсинки", "Helsinki");
        city(jdbc, TAMPERE, FINLAND, "Тампере", "Tampere");

        jdbc.update(
                "INSERT INTO person (id, real_name, notes) VALUES (?, 'John Doe', 'Coder since 1984'), (?, NULL, NULL), (?, 'Carl Real', NULL), (?, NULL, NULL)",
                ZED,
                BOB,
                CARL,
                NAMELESS);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew'), (?, 'Band')", CREW, BAND);
        jdbc.update("""
                INSERT INTO label (id, name, person_id, group_id, is_display_name) VALUES
                    (?, 'Zed', ?, NULL, TRUE), (?, 'Axe', ?, NULL, FALSE),
                    (?, 'Mona', ?, NULL, FALSE), (?, 'Bob', ?, NULL, FALSE),
                    (gen_random_uuid(), 'Crew Label', NULL, ?, TRUE)
                """, ZED_LABEL, ZED, AXE_LABEL, ZED, MONA_LABEL, BOB, BOB_LABEL, BOB, CREW);
        jdbc.update(
                "INSERT INTO group_member (group_id, label_id) VALUES (?, ?), (?, ?), (?, ?)",
                CREW,
                AXE_LABEL,
                CREW,
                ZED_LABEL,
                BAND,
                ZED_LABEL);
        jdbc.update("""
                INSERT INTO entity_location (entity_type, entity_id, city_id, country_id, is_primary) VALUES
                    ('PERSON', ?, NULL, ?, FALSE), ('PERSON', ?, ?, NULL, TRUE),
                    ('PERSON', ?, NULL, ?, TRUE),
                    ('PERSON', ?, ?, ?, TRUE)
                """, ZED, NORWAY, ZED, HELSINKI, BOB, NORWAY, CARL, TAMPERE, FINLAND);
    }

    @Test
    void listsPeopleByTheRealNameAndEveryLabelOfTheirsAsAnEntryOfItsOwnButNoLabelOfAGroup() {
        Page<AuthorCatalogEntry> page = reads.findCatalogPage(null, null, null, null, FIRST_PAGE);

        assertThat(page.getContent())
                .containsExactly(
                        new AuthorCatalogEntry(null, AXE_LABEL, "Axe", "John Doe"),
                        new AuthorCatalogEntry(null, BOB_LABEL, "Bob", null),
                        new AuthorCatalogEntry(CARL, null, "Carl Real", null),
                        new AuthorCatalogEntry(ZED, null, "John Doe", "Zed"),
                        new AuthorCatalogEntry(null, MONA_LABEL, "Mona", "Bob"),
                        new AuthorCatalogEntry(null, ZED_LABEL, "Zed", "John Doe"));
        assertThat(page.getTotalElements()).isEqualTo(6);
    }

    @Test
    void listsALabelOfNobodyAsAnEntryOfItsOwnAmongThePeople() {
        jdbc.update("INSERT INTO label (id, name) VALUES (?, 'Ghost')", GHOST);

        assertThat(reads.findCatalogPage(null, null, null, null, FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name)
                .containsExactly("Axe", "Bob", "Carl Real", "Ghost", "John Doe", "Mona", "Zed");
        assertThat(reads.findCatalogPage("G", null, null, null, FIRST_PAGE).getContent())
                .containsExactly(new AuthorCatalogEntry(null, GHOST, "Ghost", null));
    }

    @Test
    void keepsALabelOfNobodyForAPlatformItIsCreditedOn() {
        jdbc.update("INSERT INTO label (id, name) VALUES (?, 'Ghost'), (?, 'Shadow')", GHOST, SHADOW);
        jdbc.update("INSERT INTO software (id, title, platform_code) VALUES (?, 'Pong', 'vector')", id(404));
        jdbc.update("INSERT INTO software_contribution (software_id, label_id) VALUES (?, ?)", id(404), GHOST);

        assertThat(reads.findCatalogPage(null, null, null, "vector", FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name)
                .containsExactly("Ghost");
    }

    @Test
    void leavesTheLabelsOfNobodyOutOfAPlace() {
        jdbc.update("INSERT INTO label (id, name) VALUES (?, 'Ghost')", GHOST);

        assertThat(reads.findCatalogPage(null, FINLAND, null, null, FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name)
                .doesNotContain("Ghost");
    }

    @Test
    void namesTheMainLabelNextToAPersonAndTheRealNameOrElseTheMainNameNextToTheirLabels() {
        Page<AuthorCatalogEntry> page = reads.findCatalogPage(null, null, null, null, FIRST_PAGE);

        assertThat(page.getContent())
                .extracting(AuthorCatalogEntry::name, AuthorCatalogEntry::note)
                .containsExactly(
                        tuple("Axe", "John Doe"),
                        tuple("Bob", null),
                        tuple("Carl Real", null),
                        tuple("John Doe", "Zed"),
                        tuple("Mona", "Bob"),
                        tuple("Zed", "John Doe"));
    }

    @Test
    void readsTheRequestedPage() {
        Page<AuthorCatalogEntry> page = reads.findCatalogPage(null, null, null, null, PageRequest.of(1, 2));

        assertThat(page.getContent()).extracting(AuthorCatalogEntry::name).containsExactly("Carl Real", "John Doe");
        assertThat(page.getTotalElements()).isEqualTo(6);
    }

    @Test
    void filtersByTheFirstLetterInAnyCase() {
        jdbc.update("INSERT INTO label (name, person_id) VALUES ('mike', ?)", CARL);

        assertThat(reads.findCatalogPage("M", null, null, null, FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name)
                .containsExactly("mike", "Mona");
    }

    @Test
    void filtersByACyrillicLetterCountingYoAsYe() {
        jdbc.update(
                "INSERT INTO label (name, person_id) VALUES ('Ёжик', ?), ('Енот', ?), ('Борис', ?)", CARL, CARL, CARL);

        assertThat(reads.findCatalogPage("Е", null, null, null, FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name)
                .containsExactlyInAnyOrder("Ёжик", "Енот");
    }

    @Test
    void filtersNamesStartingWithAnythingElseByTheRestSign() {
        jdbc.update(
                "INSERT INTO label (name, person_id) VALUES ('8bit', ?), ('Ärger', ?), ('_under', ?), ('Борис', ?)",
                CARL,
                CARL,
                CARL,
                CARL);

        assertThat(reads.findCatalogPage("#", null, null, null, FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name)
                .containsExactlyInAnyOrder("8bit", "Ärger", "_under");
    }

    @Test
    void filtersByACountryOfAnyLocationGivenDirectlyOrThroughACity() {
        assertThat(reads.findCatalogPage(null, FINLAND, null, null, FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name)
                .containsExactly("Axe", "Carl Real", "John Doe", "Zed");
        assertThat(reads.findCatalogPage(null, NORWAY, null, null, FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name)
                .containsExactly("Axe", "Bob", "John Doe", "Mona", "Zed");
    }

    @Test
    void filtersByACity() {
        assertThat(reads.findCatalogPage(null, null, TAMPERE, null, FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name)
                .containsExactly("Carl Real");
    }

    @Test
    void combinesTheFilters() {
        assertThat(reads.findCatalogPage("Z", FINLAND, HELSINKI, null, FIRST_PAGE)
                        .getContent())
                .containsExactly(new AuthorCatalogEntry(null, ZED_LABEL, "Zed", "John Doe"));
    }

    @Test
    void keepsOnlyThePeopleCreditedOnSoftwareOfThePlatformWithAllTheirEntries() {
        credit();

        assertThat(reads.findCatalogPage(null, null, null, "vector", FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name)
                .containsExactly("Axe", "John Doe", "Zed");
        assertThat(reads.findCatalogPage(null, null, null, "bk", FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name)
                .containsExactly("Bob", "Mona");
    }

    @Test
    void keepsNobodyForAPlatformWithoutCreditedPeople() {
        credit();

        assertThat(reads.findCatalogPage(null, null, null, "msx", FIRST_PAGE).getContent())
                .isEmpty();
    }

    @Test
    void keepsAPersonCreditedByThemselvesOnSoftwareOfThePlatform() {
        credit();
        jdbc.update("INSERT INTO software_contribution (software_id, person_id) VALUES (?, ?)", id(403), CARL);

        assertThat(reads.findCatalogPage(null, null, null, "msx", FIRST_PAGE).getContent())
                .extracting(AuthorCatalogEntry::name)
                .containsExactly("Carl Real");
    }

    @Test
    void combinesThePlatformWithTheOtherFilters() {
        credit();

        assertThat(reads.findCatalogPage("Z", NORWAY, null, "vector", FIRST_PAGE)
                        .getContent())
                .extracting(AuthorCatalogEntry::name)
                .containsExactly("Zed");
    }

    @Test
    void readsTheDetailsOfAnAuthorWithTheGroupsUnderTheirMainNameAndTheLabelOfEveryMembership() {
        assertThat(reads.findById(ZED, "en"))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("added")
                .isEqualTo(new AuthorDetails(
                        ZED,
                        "Zed",
                        "John Doe",
                        "Coder since 1984",
                        List.of(new AuthorLabel(AXE_LABEL, "Axe", false), new AuthorLabel(ZED_LABEL, "Zed", true)),
                        List.of(
                                new AuthorLocation(FINLAND, "Finland", HELSINKI, "Helsinki", true),
                                new AuthorLocation(NORWAY, "Norway", null, null, false)),
                        List.of(
                                new AuthorGroup(BAND, "Band", null),
                                new AuthorGroup(CREW, "Crew", "Axe"),
                                new AuthorGroup(CREW, "Crew", null)),
                        ADDED));
    }

    @Test
    void namesTheLocationsOfAnAuthorInTheLanguageAskedFor() {
        assertThat(reads.findById(ZED, "ru").orElseThrow().locations())
                .containsExactly(
                        new AuthorLocation(FINLAND, "Финляндия", HELSINKI, "Хельсинки", true),
                        new AuthorLocation(NORWAY, "Норвегия", null, null, false));
    }

    @Test
    void readsAnAuthorKnownOnlyByTheRealName() {
        assertThat(reads.findById(CARL, "en"))
                .get()
                .usingRecursiveComparison()
                .ignoringFields("added")
                .isEqualTo(new AuthorDetails(
                        CARL,
                        "Carl Real",
                        "Carl Real",
                        null,
                        List.of(),
                        List.of(new AuthorLocation(FINLAND, "Finland", TAMPERE, "Tampere", true)),
                        List.of(),
                        ADDED));
    }

    @Test
    void findsNothingForANamelessPerson() {
        assertThat(reads.findById(NAMELESS, "en")).isEmpty();
    }

    @Test
    void findsNothingForAnUnknownId() {
        assertThat(reads.findById(UUID.randomUUID(), "en")).isEmpty();
    }

    /**
     * Elite on Vector is credited to Zed and to Crew Label, a group; Tetris on BK to Mona; Lines on MSX to Crew
     * Label only.
     */
    private void credit() {
        UUID elite = id(401);
        UUID tetris = id(402);
        UUID lines = id(403);
        jdbc.update(
                "INSERT INTO software (id, title, platform_code) VALUES (?, 'Elite', 'vector'), (?, 'Tetris', 'bk'), (?, 'Lines', 'msx')",
                elite,
                tetris,
                lines);
        jdbc.update("""
                INSERT INTO software_contribution (software_id, label_id)
                SELECT s, l.id FROM label l, (VALUES (?::uuid, 'Zed'), (?::uuid, 'Crew Label'), (?::uuid, 'Mona'),
                    (?::uuid, 'Crew Label')) AS c(s, name)
                WHERE l.name = c.name
                """, elite, elite, tetris, lines);
    }

    private static UUID id(int n) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n));
    }

    @Test
    void readsWhoAddedThePerson() {
        TestActors.store(jdbc);
        jdbc.update("UPDATE person SET added_by = ? WHERE id = ?", MEMBER_ID.value(), ZED);

        assertThat(reads.findById(ZED, "en"))
                .hasValueSatisfying(author -> assertThat(author.addedBy()).isEqualTo(MEMBER_ID.value()));
    }
}
