package com.archive.backend.software.infra;

import static com.archive.backend.user.TestActors.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.software.read.ReleaseDetails;
import com.archive.backend.software.read.SoftwareCategory;
import com.archive.backend.software.read.SoftwareCategoryStep;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareHardware;
import com.archive.backend.software.read.SoftwarePlatform;
import com.archive.backend.software.read.SoftwareRelease;
import com.archive.backend.software.read.SoftwareScreenshot;
import com.archive.backend.software.read.SoftwareSource;
import com.archive.backend.software.read.SoftwareSummary;
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
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcSoftwareReadRepository.class})
class JdbcSoftwareReadRepositoryIT {

    /** Vector, added in 2022; base hardware of three groups. */
    private static final UUID ELITE = id(1);
    /** BK, added in 2020. */
    private static final UUID TETRIS = id(2);
    /** Vector, added in 2024. */
    private static final UUID NEWEST = id(3);

    /** No title, no date, no hardware of its own. */
    private static final UUID ORIGINAL = id(11);
    /** Its own sound. */
    private static final UUID AY_MOD = id(12);
    /** The oldest. */
    private static final UUID FIRST = id(13);
    /** Only its year is known: the year of the AY mod. */
    private static final UUID DISK = id(14);

    private static final SoftwareHardware VECTOR06C = new SoftwareHardware("vector06c", "Vector-06C", "computers");
    private static final SoftwareHardware TAPE = new SoftwareHardware("tape", "Tape", "storage");
    private static final SoftwareHardware BEEPER = new SoftwareHardware("beeper", "Beeper", "sound");
    private static final SoftwareHardware AY = new SoftwareHardware("ay8910", "AY-3-8910", "sound");
    private static final SoftwareHardware JOYSTICK = new SoftwareHardware("joystick", "Joystick", "controls");

    @Autowired
    private JdbcSoftwareReadRepository reads;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void insert() {
        jdbc.update("""
                INSERT INTO software (id, title, description, release_year, release_day, platform_code, created_at) VALUES
                    (?, 'Elite', 'Space trading', 1985, DATE '1985-09-20', 'vector', TIMESTAMPTZ '2022-01-01 00:00Z'),
                    (?, 'Tetris', NULL, 1989, NULL, 'bk', TIMESTAMPTZ '2020-01-01 00:00Z'),
                    (?, 'Newest', NULL, NULL, NULL, 'vector', TIMESTAMPTZ '2024-01-01 00:00Z')
                """, ELITE, TETRIS, NEWEST);
        jdbc.update("""
                INSERT INTO software_hardware (software_id, hardware_code) VALUES
                    (?, 'joystick'), (?, 'beeper'), (?, 'tape'), (?, 'vector06c')
                """, ELITE, ELITE, ELITE, ELITE);
        jdbc.update("""
                INSERT INTO software_release
                    (id, software_id, title, version, type, release_year, release_day, description) VALUES
                    (?, ?, NULL, NULL, 'original', NULL, NULL, NULL),
                    (?, ?, 'Elite AY', '1.1', 'mod', 1990, DATE '1990-03-01', 'With AY music'),
                    (?, ?, 'Elite Beta', '0.9', 'demoversion', 1985, DATE '1985-01-01', NULL),
                    (?, ?, 'Elite Disk', NULL, 'rerelease', 1990, NULL, NULL)
                """, ORIGINAL, ELITE, AY_MOD, ELITE, FIRST, ELITE, DISK, ELITE);
        jdbc.update("INSERT INTO software_release_hardware (release_id, hardware_code) VALUES (?, 'ay8910')", AY_MOD);
    }

    @Test
    void readsTheLatestAddedSoftwareFirstWhateverSortIsAsked() {
        Page<SoftwareSummary> page = reads.findLatest(null, PageRequest.of(0, 2, Sort.by("title")));

        assertThat(page.getContent()).extracting(SoftwareSummary::title).containsExactly("Newest", "Elite");
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void readsTheLatestAddedSoftwareOfAPlatform() {
        Page<SoftwareSummary> page = reads.findLatest("bk", PageRequest.of(0, 2));

        assertThat(page.getContent()).extracting(SoftwareSummary::title).containsExactly("Tetris");
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    private static SoftwareScreenshot shot(int n) {
        return new SoftwareScreenshot(UUID.fromString("00000000-0000-0000-0055-%012d".formatted(n)));
    }

    /** Stores the screenshots of the software, in the order given. */
    private void softwareShots(UUID software, SoftwareScreenshot... shots) {
        for (int i = 0; i < shots.length; i++) {
            jdbc.update(
                    "INSERT INTO software_screenshot (software_id, position, id, format) VALUES (?, ?, ?, 'png')",
                    software,
                    i,
                    shots[i].id());
        }
    }

    private void releaseShots(UUID release, SoftwareScreenshot... shots) {
        for (int i = 0; i < shots.length; i++) {
            jdbc.update(
                    "INSERT INTO software_release_screenshot (release_id, position, id, format) VALUES (?, ?, ?, 'png')",
                    release,
                    i,
                    shots[i].id());
        }
    }

    private List<SoftwareScreenshot> cardOf(UUID software) {
        return reads.findLatest(null, PageRequest.of(0, 10)).getContent().stream()
                .filter(summary -> summary.id().equals(software))
                .findFirst()
                .orElseThrow()
                .screenshots();
    }

    @Test
    void readsTheScreenshotsOfSoftwareAndOfAReleaseInTheirOrder() {
        // Positions stored out of the order of the ids.
        softwareShots(ELITE, shot(2), shot(1), shot(3));
        releaseShots(AY_MOD, shot(5), shot(4));

        assertThat(reads.findScreenshots(ELITE)).containsExactly(shot(2), shot(1), shot(3));
        assertThat(reads.findReleaseScreenshots(AY_MOD)).containsExactly(shot(5), shot(4));
        assertThat(reads.findReleaseScreenshots(ORIGINAL)).isEmpty();
        assertThat(reads.findScreenshots(TETRIS)).isEmpty();
    }

    @Test
    void readsTheScreenshotsOfEveryRelease() {
        releaseShots(AY_MOD, shot(5), shot(4));

        assertThat(reads.findReleases(ELITE))
                .extracting(SoftwareRelease::id, SoftwareRelease::screenshots)
                .containsExactly(
                        tuple(FIRST, List.of()),
                        tuple(DISK, List.of()),
                        tuple(AY_MOD, List.of(shot(5), shot(4))),
                        tuple(ORIGINAL, List.of()));
    }

    @Test
    void theCardShowsTheScreenshotsOfTheReleasesInTheirOrderWhenTheSoftwareHasNoneOfItsOwn() {
        releaseShots(ORIGINAL, shot(6));
        releaseShots(AY_MOD, shot(5), shot(4));
        releaseShots(FIRST, shot(7));

        assertThat(cardOf(ELITE)).containsExactly(shot(7), shot(5), shot(4), shot(6));
        assertThat(cardOf(TETRIS)).isEmpty();
    }

    @Test
    void theCardShowsOnlyTheOwnScreenshotsOfSoftwareThatHasThem() {
        releaseShots(AY_MOD, shot(5));
        softwareShots(ELITE, shot(2), shot(1));

        assertThat(cardOf(ELITE)).containsExactly(shot(2), shot(1));
    }

    private List<String> madeByOf(UUID software) {
        return reads.findLatest(null, PageRequest.of(0, 10)).getContent().stream()
                .filter(summary -> summary.id().equals(software))
                .findFirst()
                .orElseThrow()
                .madeBy();
    }

    @Test
    void theCardNamesTheDeveloperCompaniesElseTheCreditedAuthorsByName() {
        UUID crew = id(21);
        UUID band = id(22);
        UUID john = id(23);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew'), (?, 'Band')", crew, band);
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'John Doe')", john);
        jdbc.update("""
                INSERT INTO label (id, name, person_id, group_id, is_display_name) VALUES
                    (?, 'The Crew', NULL, ?, true),
                    (?, 'zeta', NULL, ?, false),
                    (?, 'JD', ?, NULL, true),
                    (?, 'Ace', NULL, NULL, false)
                """, id(31), crew, id(32), band, id(33), john, id(34));
        jdbc.update("INSERT INTO software_developer_group (software_id, group_id) VALUES (?, ?)", ELITE, crew);
        jdbc.update("INSERT INTO software_developer_label (software_id, label_id) VALUES (?, ?)", ELITE, id(32));
        jdbc.update("""
                INSERT INTO software_contribution (software_id, person_id, label_id) VALUES
                    (?, ?, NULL), (?, ?, NULL), (?, NULL, ?)
                """, ELITE, john, TETRIS, john, TETRIS, id(34));

        assertThat(madeByOf(ELITE)).containsExactly("The Crew", "zeta");
        assertThat(madeByOf(TETRIS)).containsExactly("Ace", "JD");
        assertThat(madeByOf(NEWEST)).isEmpty();
    }

    @Test
    void readsTheDetailsWithThePlatformAndTheHardwareInTheOrderOfTheGroups() {
        assertThat(reads.findById(ELITE))
                .contains(new SoftwareDetails(
                        ELITE,
                        "Elite",
                        "Space trading",
                        "1985-09-20",
                        new SoftwarePlatform("vector", "Vector"),
                        List.of(VECTOR06C, TAPE, BEEPER, JOYSTICK),
                        List.of()));
    }

    @Test
    void readsThePagesOfTheArchivesTheSoftwareWasImportedFrom() {
        String page = "https://vektor-06c.ru/programmnoe-obespechenie/arkadnye-igry/elite/";
        jdbc.update(
                "INSERT INTO external_origin (origin, kind, external_id, entity_id, url) VALUES ('vektor-06c.ru', 'SOFTWARE', '7', ?, ?)",
                ELITE,
                page);
        jdbc.update(
                "INSERT INTO external_origin (origin, kind, external_id, entity_id) VALUES ('vektor-06c.ru', 'LABEL', 'Elite', ?)",
                ELITE);

        assertThat(reads.findById(ELITE))
                .hasValueSatisfying(details ->
                        assertThat(details.sources()).containsExactly(new SoftwareSource("vektor-06c.ru", page)));
        assertThat(reads.findById(TETRIS))
                .hasValueSatisfying(details -> assertThat(details.sources()).isEmpty());
    }

    @Test
    void readsTheYearOfSoftwareOfWhichOnlyTheYearIsKnown() {
        assertThat(reads.findById(TETRIS))
                .hasValueSatisfying(details -> assertThat(details.releaseDate()).isEqualTo("1989"));
    }

    @Test
    void readsTheYearOfAReleaseOfWhichOnlyTheYearIsKnown() {
        assertThat(reads.findRelease(ELITE, DISK))
                .hasValueSatisfying(details -> assertThat(details.releaseDate()).isEqualTo("1990"));
    }

    @Test
    void findsNothingForAnUnknownId() {
        assertThat(reads.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void readsTheReleasesOldestFirstWithOnlyTheirOwnHardware() {
        assertThat(reads.findReleases(ELITE))
                .containsExactly(
                        new SoftwareRelease(FIRST, "Elite Beta", "0.9", "demoversion", "1985-01-01", null, List.of()),
                        new SoftwareRelease(DISK, "Elite Disk", null, "rerelease", "1990", null, List.of()),
                        new SoftwareRelease(
                                AY_MOD, "Elite AY", "1.1", "mod", "1990-03-01", "With AY music", List.of(AY)),
                        new SoftwareRelease(ORIGINAL, "Elite", null, "original", null, null, List.of()));
    }

    @Test
    void readsNoReleasesForSoftwareWithoutAny() {
        assertThat(reads.findReleases(TETRIS)).isEmpty();
    }

    private static UUID id(int n) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n));
    }

    @Test
    void readsAReleaseAsItStatesItself() {
        assertThat(reads.findRelease(ELITE, AY_MOD))
                .contains(new ReleaseDetails(
                        AY_MOD, "Elite AY", "1.1", "mod", "1990-03-01", "With AY music", List.of(AY)));
    }

    @Test
    void readsAReleaseWithoutATitleOfItsOwnOrHardware() {
        assertThat(reads.findRelease(ELITE, ORIGINAL))
                .contains(new ReleaseDetails(ORIGINAL, null, null, "original", null, null, List.of()));
    }

    @Test
    void readsNoReleaseOfOtherSoftware() {
        assertThat(reads.findRelease(TETRIS, AY_MOD)).isEmpty();
    }

    @Test
    void readsTheCategoriesInTheOrderOfTheTree() {
        // Demoscene → Intro → 4K intro, Games → Action, Games; inserted out of order.
        UUID intro4k = UUID.fromString("e9d495af-b0d1-5b0f-a22f-6955b423ab8d");
        UUID games = UUID.fromString("081bf3a1-782b-5a8b-8d12-3b155e0a58cb");
        UUID action = jdbc.queryForObject(
                "SELECT id FROM category WHERE parent_id = ? AND title = 'Action'", UUID.class, games);
        jdbc.update(
                "INSERT INTO software_category (software_id, category_id) VALUES (?, ?), (?, ?), (?, ?)",
                ELITE,
                intro4k,
                ELITE,
                action,
                ELITE,
                games);

        assertThat(reads.findById(ELITE))
                .hasValueSatisfying(details -> assertThat(details.categories())
                        .extracting(SoftwareCategory::title)
                        .containsExactly("Games", "Action", "4K intro"));
    }

    @Test
    void readsEveryCategoryWithItsBranchFromTheTopOfTheTree() {
        UUID intro4k = UUID.fromString("e9d495af-b0d1-5b0f-a22f-6955b423ab8d");
        UUID intro = UUID.fromString("47301d2f-eb74-5c1c-90c3-090b4f33e498");
        UUID demoscene = UUID.fromString("c35b7f9b-44a8-5570-9406-9e6dcb30b607");
        UUID games = UUID.fromString("081bf3a1-782b-5a8b-8d12-3b155e0a58cb");
        jdbc.update(
                "INSERT INTO software_category (software_id, category_id) VALUES (?, ?), (?, ?)",
                ELITE,
                intro4k,
                ELITE,
                games);

        assertThat(reads.findById(ELITE))
                .hasValueSatisfying(details -> assertThat(details.categories())
                        .containsExactly(
                                new SoftwareCategory(games, "Games", List.of(new SoftwareCategoryStep(games, "Games"))),
                                new SoftwareCategory(
                                        intro4k,
                                        "4K intro",
                                        List.of(
                                                new SoftwareCategoryStep(demoscene, "Demoscene"),
                                                new SoftwareCategoryStep(intro, "Intro"),
                                                new SoftwareCategoryStep(intro4k, "4K intro")))));
    }

    @Test
    void readsWhoAddedTheSoftwareAndEachRelease() {
        TestActors.store(jdbc);
        jdbc.update("UPDATE software SET added_by = ? WHERE id = ?", MEMBER_ID.value(), ELITE);
        jdbc.update("UPDATE software_release SET added_by = ? WHERE id = ?", MEMBER_ID.value(), AY_MOD);

        assertThat(reads.findById(ELITE))
                .hasValueSatisfying(software -> assertThat(software.addedBy()).isEqualTo(MEMBER_ID.value()));
        assertThat(reads.findById(TETRIS))
                .hasValueSatisfying(software -> assertThat(software.addedBy()).isNull());
        assertThat(reads.findReleases(ELITE))
                .filteredOn(release -> release.id().equals(AY_MOD))
                .extracting(SoftwareRelease::addedBy)
                .containsExactly(MEMBER_ID.value());
        assertThat(reads.findReleases(ELITE))
                .filteredOn(release -> !release.id().equals(AY_MOD))
                .extracting(SoftwareRelease::addedBy)
                .containsOnlyNulls();
    }
}
