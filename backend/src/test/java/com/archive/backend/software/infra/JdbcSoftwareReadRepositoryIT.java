package com.archive.backend.software.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.software.read.ReleaseDetails;
import com.archive.backend.software.read.SoftwareCategory;
import com.archive.backend.software.read.SoftwareCategoryStep;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareHardware;
import com.archive.backend.software.read.SoftwarePlatform;
import com.archive.backend.software.read.SoftwareRelease;
import com.archive.backend.software.read.SoftwareSummary;
import java.time.LocalDate;
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
                INSERT INTO software (id, title, description, release_date, platform_code, created_at) VALUES
                    (?, 'Elite', 'Space trading', DATE '1985-09-20', 'vector', TIMESTAMPTZ '2022-01-01 00:00Z'),
                    (?, 'Tetris', NULL, NULL, 'bk', TIMESTAMPTZ '2020-01-01 00:00Z'),
                    (?, 'Newest', NULL, NULL, 'vector', TIMESTAMPTZ '2024-01-01 00:00Z')
                """, ELITE, TETRIS, NEWEST);
        jdbc.update("""
                INSERT INTO software_hardware (software_id, hardware_code) VALUES
                    (?, 'joystick'), (?, 'beeper'), (?, 'tape'), (?, 'vector06c')
                """, ELITE, ELITE, ELITE, ELITE);
        jdbc.update("""
                INSERT INTO software_release (id, software_id, title, version, type, release_date, description) VALUES
                    (?, ?, NULL, NULL, 'original', NULL, NULL),
                    (?, ?, 'Elite AY', '1.1', 'mod', DATE '1990-03-01', 'With AY music'),
                    (?, ?, 'Elite Beta', '0.9', 'demoversion', DATE '1985-01-01', NULL)
                """, ORIGINAL, ELITE, AY_MOD, ELITE, FIRST, ELITE);
        jdbc.update("INSERT INTO software_release_hardware (release_id, hardware_code) VALUES (?, 'ay8910')", AY_MOD);
    }

    @Test
    void readsAPageOfSummariesOfEveryPlatformByTitle() {
        Page<SoftwareSummary> page = reads.findPage(null, PageRequest.of(0, 20));

        assertThat(page.getContent())
                .containsExactly(
                        new SoftwareSummary(ELITE, "Elite", LocalDate.of(1985, 9, 20)),
                        new SoftwareSummary(NEWEST, "Newest", null),
                        new SoftwareSummary(TETRIS, "Tetris", null));
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void readsAPageOfTheSoftwareOfAPlatform() {
        Page<SoftwareSummary> page = reads.findPage("vector", PageRequest.of(0, 20));

        assertThat(page.getContent()).extracting(SoftwareSummary::title).containsExactly("Elite", "Newest");
        assertThat(page.getTotalElements()).isEqualTo(2);
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

    @Test
    void readsTheDetailsWithThePlatformAndTheHardwareInTheOrderOfTheGroups() {
        assertThat(reads.findById(ELITE))
                .contains(new SoftwareDetails(
                        ELITE,
                        "Elite",
                        "Space trading",
                        LocalDate.of(1985, 9, 20),
                        new SoftwarePlatform("vector", "Vector"),
                        List.of(VECTOR06C, TAPE, BEEPER, JOYSTICK),
                        List.of()));
    }

    @Test
    void findsNothingForAnUnknownId() {
        assertThat(reads.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void readsTheReleasesOldestFirstWithOnlyTheirOwnHardware() {
        assertThat(reads.findReleases(ELITE))
                .containsExactly(
                        new SoftwareRelease(
                                FIRST, "Elite Beta", "0.9", "demoversion", LocalDate.of(1985, 1, 1), null, List.of()),
                        new SoftwareRelease(
                                AY_MOD,
                                "Elite AY",
                                "1.1",
                                "mod",
                                LocalDate.of(1990, 3, 1),
                                "With AY music",
                                List.of(AY)),
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
                        AY_MOD, "Elite AY", "1.1", "mod", LocalDate.of(1990, 3, 1), "With AY music", List.of(AY)));
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
}
