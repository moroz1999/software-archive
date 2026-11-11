package com.archive.backend.group.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.group.read.GroupCatalogEntry;
import com.archive.backend.group.read.GroupDetails;
import com.archive.backend.group.read.GroupLabel;
import com.archive.backend.group.read.GroupLocation;
import com.archive.backend.group.read.GroupMember;
import com.archive.backend.group.read.RelatedGroup;
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
@Import({TestcontainersConfiguration.class, JdbcGroupReadRepository.class})
class JdbcGroupReadRepositoryIT {

    private static final PageRequest FIRST_PAGE = PageRequest.of(0, 100);

    private static final UUID FINLAND = id(1);
    private static final UUID NORWAY = id(2);
    private static final UUID HELSINKI = id(11);
    private static final UUID TAMPERE = id(12);

    /**
     * Named Crew Ltd, known by the labels Crew (display name) and CRW; a demogroup and a label; located in Helsinki
     * (primary) and Norway. Members: хакер федя (a label of Fedya, who is MASTERSOFT now), Zed (the main name of Zed)
     * and Ghost (a label of nobody known). Band is a group inside it; it is inside Oldies.
     */
    private static final UUID CREW = id(301);
    /** Known only by its name; located in Tampere. */
    private static final UUID BAND = id(302);
    /** Named Oldies, known by the labels Zeta and Alpha, none marked: Alpha is the main name. */
    private static final UUID OLDIES = id(303);

    private static final UUID CREW_LABEL = id(201);
    private static final UUID CRW_LABEL = id(202);
    private static final UUID FEDYA = id(101);
    private static final UUID ZED = id(102);

    @Autowired
    private JdbcGroupReadRepository reads;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void insert() {
        jdbc.update("INSERT INTO country (id, name) VALUES (?, 'Finland'), (?, 'Norway')", FINLAND, NORWAY);
        jdbc.update(
                "INSERT INTO city (id, name, country_id) VALUES (?, 'Helsinki', ?), (?, 'Tampere', ?)",
                HELSINKI,
                FINLAND,
                TAMPERE,
                FINLAND);

        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Fyodor Ivanko'), (?, NULL)", FEDYA, ZED);
        jdbc.update(
                "INSERT INTO \"group\" (id, name, notes) VALUES (?, 'Crew Ltd', 'Since 1990'), (?, 'Band', NULL), (?, 'Oldies', NULL)",
                CREW,
                BAND,
                OLDIES);
        jdbc.update(
                "INSERT INTO group_group_type (group_id, group_type_code) VALUES (?, 'label'), (?, 'demogroup')",
                CREW,
                CREW);
        jdbc.update("""
                INSERT INTO label (id, name, person_id, group_id, is_display_name) VALUES
                    (?, 'Crew', NULL, ?, TRUE), (?, 'CRW', NULL, ?, FALSE),
                    (gen_random_uuid(), 'Zeta', NULL, ?, FALSE), (gen_random_uuid(), 'Alpha', NULL, ?, FALSE)
                """, CREW_LABEL, CREW, CRW_LABEL, CREW, OLDIES, OLDIES);
        jdbc.update("""
                INSERT INTO label (id, name, person_id, group_id, is_display_name) VALUES
                    (?, 'MASTERSOFT', ?, NULL, TRUE), (?, 'хакер федя', ?, NULL, FALSE),
                    (?, 'Zed', ?, NULL, FALSE), (?, 'Ghost', NULL, NULL, FALSE)
                """, id(211), FEDYA, id(212), FEDYA, id(213), ZED, id(214));
        jdbc.update(
                "INSERT INTO group_member (group_id, label_id) VALUES (?, ?), (?, ?), (?, ?)",
                CREW,
                id(212),
                CREW,
                id(213),
                CREW,
                id(214));
        jdbc.update(
                "INSERT INTO group_member_group (group_id, member_group_id) VALUES (?, ?), (?, ?)",
                CREW,
                BAND,
                OLDIES,
                CREW);
        jdbc.update("""
                INSERT INTO entity_location (entity_type, entity_id, city_id, country_id, is_primary) VALUES
                    ('GROUP', ?, NULL, ?, FALSE), ('GROUP', ?, ?, NULL, TRUE),
                    ('GROUP', ?, ?, ?, TRUE)
                """, CREW, NORWAY, CREW, HELSINKI, BAND, TAMPERE, FINLAND);
    }

    @Test
    void listsGroupsUnderTheirMainNameAndTheirOtherLabelsByNameButNoLabelOfAPersonOrOfNobody() {
        Page<GroupCatalogEntry> page = reads.findCatalogPage(null, null, null, null, FIRST_PAGE);

        assertThat(page.getContent())
                .containsExactly(
                        new GroupCatalogEntry(OLDIES, "Alpha", "Oldies"),
                        new GroupCatalogEntry(BAND, "Band", null),
                        new GroupCatalogEntry(CREW, "Crew", "Crew Ltd"),
                        new GroupCatalogEntry(CREW, "CRW", "Crew Ltd"),
                        new GroupCatalogEntry(OLDIES, "Zeta", "Oldies"));
        assertThat(page.getTotalElements()).isEqualTo(5);
    }

    @Test
    void namesNoGroupNameNextToALabelThatIsTheGroupName() {
        jdbc.update("INSERT INTO label (name, group_id) VALUES ('Oldies', ?)", OLDIES);

        assertThat(reads.findCatalogPage("O", null, null, null, FIRST_PAGE).getContent())
                .containsExactly(new GroupCatalogEntry(OLDIES, "Oldies", null));
    }

    @Test
    void readsTheRequestedPage() {
        Page<GroupCatalogEntry> page = reads.findCatalogPage(null, null, null, null, PageRequest.of(1, 2));

        assertThat(page.getContent()).extracting(GroupCatalogEntry::name).containsExactly("Crew", "CRW");
        assertThat(page.getTotalElements()).isEqualTo(5);
    }

    @Test
    void filtersByTheFirstLetterInAnyCase() {
        assertThat(reads.findCatalogPage("C", null, null, null, FIRST_PAGE).getContent())
                .extracting(GroupCatalogEntry::name)
                .containsExactly("Crew", "CRW");
    }

    @Test
    void filtersByACyrillicLetterCountingYoAsYeAndTheRestSign() {
        jdbc.update(
                "INSERT INTO label (name, group_id) VALUES ('Ёлки', ?), ('Енисей', ?), ('8bit', ?), ('Борис', ?)",
                BAND,
                BAND,
                BAND,
                BAND);

        assertThat(reads.findCatalogPage("Е", null, null, null, FIRST_PAGE).getContent())
                .extracting(GroupCatalogEntry::name)
                .containsExactlyInAnyOrder("Ёлки", "Енисей");
        assertThat(reads.findCatalogPage("#", null, null, null, FIRST_PAGE).getContent())
                .extracting(GroupCatalogEntry::name)
                .containsExactly("8bit");
    }

    @Test
    void filtersByACountryOfAnyLocationGivenDirectlyOrThroughACity() {
        assertThat(reads.findCatalogPage(null, FINLAND, null, null, FIRST_PAGE).getContent())
                .extracting(GroupCatalogEntry::name)
                .containsExactly("Band", "Crew", "CRW");
        assertThat(reads.findCatalogPage(null, NORWAY, null, null, FIRST_PAGE).getContent())
                .extracting(GroupCatalogEntry::name)
                .containsExactly("Crew", "CRW");
    }

    @Test
    void filtersByACity() {
        assertThat(reads.findCatalogPage(null, null, TAMPERE, null, FIRST_PAGE).getContent())
                .extracting(GroupCatalogEntry::name)
                .containsExactly("Band");
    }

    @Test
    void combinesTheFilters() {
        assertThat(reads.findCatalogPage("C", FINLAND, HELSINKI, null, FIRST_PAGE)
                        .getContent())
                .extracting(GroupCatalogEntry::name)
                .containsExactly("Crew", "CRW");
    }

    @Test
    void keepsOnlyTheGroupsCreditedOnSoftwareOfThePlatformWithAllTheirEntries() {
        credit();

        assertThat(reads.findCatalogPage(null, null, null, "vector", FIRST_PAGE).getContent())
                .extracting(GroupCatalogEntry::name)
                .containsExactly("Crew", "CRW");
        assertThat(reads.findCatalogPage(null, null, null, "bk", FIRST_PAGE).getContent())
                .extracting(GroupCatalogEntry::name)
                .containsExactly("Alpha", "Zeta");
    }

    @Test
    void keepsNoGroupForAPlatformWithoutCreditedGroups() {
        credit();

        assertThat(reads.findCatalogPage(null, null, null, "msx", FIRST_PAGE).getContent())
                .isEmpty();
    }

    @Test
    void combinesThePlatformWithTheOtherFilters() {
        credit();

        assertThat(reads.findCatalogPage("C", null, HELSINKI, "vector", FIRST_PAGE)
                        .getContent())
                .extracting(GroupCatalogEntry::name)
                .containsExactly("Crew", "CRW");
    }

    @Test
    void readsTheDetailsOfAGroup() {
        assertThat(reads.findById(CREW))
                .contains(new GroupDetails(
                        CREW,
                        "Crew",
                        "Crew Ltd",
                        "Since 1990",
                        List.of("demogroup", "label"),
                        List.of(new GroupLabel(CREW_LABEL, "Crew", true), new GroupLabel(CRW_LABEL, "CRW", false)),
                        List.of(
                                new GroupLocation(FINLAND, "Finland", HELSINKI, "Helsinki", true),
                                new GroupLocation(NORWAY, "Norway", null, null, false)),
                        List.of(
                                new GroupMember("Ghost", null, null),
                                new GroupMember("Zed", ZED, null),
                                new GroupMember("хакер федя", FEDYA, "MASTERSOFT")),
                        List.of(new RelatedGroup(BAND, "Band")),
                        List.of(new RelatedGroup(OLDIES, "Alpha"))));
    }

    @Test
    void readsAGroupKnownOnlyByItsName() {
        assertThat(reads.findById(BAND))
                .contains(new GroupDetails(
                        BAND,
                        "Band",
                        "Band",
                        null,
                        List.of(),
                        List.of(),
                        List.of(new GroupLocation(FINLAND, "Finland", TAMPERE, "Tampere", true)),
                        List.of(),
                        List.of(),
                        List.of(new RelatedGroup(CREW, "Crew"))));
    }

    @Test
    void marksTheMainNameOfAGroupFromBeforeTheRuleAsItsDisplayName() {
        assertThat(reads.findById(OLDIES).orElseThrow().labels())
                .extracting(GroupLabel::name, GroupLabel::display)
                .containsExactly(tuple("Alpha", true), tuple("Zeta", false));
    }

    @Test
    void findsNothingForAnUnknownId() {
        assertThat(reads.findById(UUID.randomUUID())).isEmpty();
    }

    /**
     * Elite on Vector is credited to CRW, a label of Crew, and to хакер федя, a member of Crew but a person's label;
     * Tetris on BK to Zeta, a label of Oldies; Lines on MSX to хакер федя only.
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
                SELECT s, l.id FROM label l, (VALUES (?::uuid, 'CRW'), (?::uuid, 'хакер федя'), (?::uuid, 'Zeta'),
                    (?::uuid, 'хакер федя')) AS c(s, name)
                WHERE l.name = c.name
                """, elite, elite, tetris, lines);
    }

    private static UUID id(int n) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n));
    }
}
