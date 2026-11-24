package com.archive.backend.search.app;

import static com.archive.backend.user.TestActors.ADMIN_ID;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.ReleaseYear;
import com.archive.backend.search.read.FoundRelease;
import com.archive.backend.software.domain.ReleaseFileHash;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareReferenceId;
import com.archive.backend.user.TestActors;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/** Releases found apart from their software (docs/domain/ai-assistant.md#release-search). */
@IntegrationTest
@Transactional
@Rollback
class SearchReleasesIT {

    private static final UUID JANE = id(1);
    private static final UUID JD = id(2);
    private static final UUID CREW = id(3);
    private static final UUID CRW = id(4);
    private static final UUID GHOST = id(5);
    private static final UUID ELITE = id(11);
    private static final UUID TETRIS = id(12);
    private static final UUID ELITE_AY = id(21);
    private static final UUID ELITE_CRACK = id(22);
    private static final UUID TETRIS_ORIGINAL = id(23);

    @Autowired
    private SearchReleasesHandler handler;

    @Autowired
    private JdbcTemplate jdbc;

    /**
     * Elite (Vector, a Vector-06C on tape) has Elite AY (adaptation, 1987, version 1.1, on a floppy in place of the
     * tape and with an AY-3-8910, the archive elite.zip with ELITE.ROM inside) and an untitled crack (1990, of the
     * software's hardware). Tetris (BK, no hardware known) has an untitled original of no date. Jane Doe is known as JD; Crew as CRW;
     * Ghost is a label of nobody. Elite AY credits Jane herself for the adaptation; the crack credits JD for the
     * release and CRW for the intro code; the Tetris original credits Crew for development and Ghost for music.
     */
    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Jane Doe')", JANE);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew')", CREW);
        jdbc.update("""
                INSERT INTO label (id, name, person_id, group_id, is_display_name) VALUES
                    (?, 'JD', ?, NULL, true), (?, 'CRW', NULL, ?, false), (?, 'Ghost', NULL, NULL, false)
                """, JD, JANE, CRW, CREW, GHOST);
        jdbc.update("""
                INSERT INTO software (id, title, platform_code, added_by) VALUES
                    (?, 'Elite', 'vector', ?), (?, 'Tetris', 'bk', ?)
                """, ELITE, ADMIN_ID.value(), TETRIS, ADMIN_ID.value());
        jdbc.update("""
                INSERT INTO software_hardware (software_id, hardware_code) VALUES
                    (?, 'vector06c'), (?, 'tape')
                """, ELITE, ELITE);
        jdbc.update(
                """
                INSERT INTO software_release
                    (id, software_id, title, version, type, release_year, archive_id, archive_file_name, archive_size,
                     added_by)
                VALUES (?, ?, 'Elite AY', '1.1', 'adaptation', 1987, ?, 'elite.zip', 1234, ?),
                       (?, ?, NULL, NULL, 'crack', 1990, NULL, NULL, NULL, ?),
                       (?, ?, NULL, NULL, 'original', NULL, NULL, NULL, NULL, ?)
                """,
                ELITE_AY,
                ELITE,
                id(31),
                ADMIN_ID.value(),
                ELITE_CRACK,
                ELITE,
                ADMIN_ID.value(),
                TETRIS_ORIGINAL,
                TETRIS,
                ADMIN_ID.value());
        jdbc.update(
                "INSERT INTO software_release_hardware (release_id, hardware_code) VALUES (?, 'floppy'), (?, 'ay8910')",
                ELITE_AY,
                ELITE_AY);
        jdbc.update("""
                INSERT INTO release_file (id, release_id, parent_id, file_name, position, md5, directory, executable)
                VALUES (?, ?, NULL, 'elite.zip', 0, '0123456789abcdef0123456789abcdef', false, false),
                       (?, ?, ?, 'ELITE.ROM', 0, 'fedcba9876543210fedcba9876543210', false, true)
                """, id(41), ELITE_AY, id(42), ELITE_AY, id(41));
        jdbc.update(
                """
                INSERT INTO software_release_contribution (release_id, person_id, group_id, label_id, role_codes)
                VALUES (?, ?, NULL, NULL, ARRAY['adaptation']), (?, NULL, NULL, ?, ARRAY['release']),
                       (?, NULL, NULL, ?, ARRAY['intro_code']), (?, NULL, ?, NULL, ARRAY['developer']),
                       (?, NULL, NULL, ?, ARRAY['music'])
                """, ELITE_AY, JANE, ELITE_CRACK, JD, ELITE_CRACK, CRW, TETRIS_ORIGINAL, CREW, TETRIS_ORIGINAL, GHOST);
    }

    @Test
    void findsEveryReleaseByTheTitleOfItsSoftwareThenByDate() {
        assertThat(ids(search().word("elite"))).containsExactly(ELITE_AY, ELITE_CRACK);
        assertThat(ids(search())).containsExactly(ELITE_AY, ELITE_CRACK, TETRIS_ORIGINAL);
    }

    @Test
    void findsAReleaseByAWordOfItsOwnTitleOrVersion() {
        assertThat(ids(search().word("AY"))).containsExactly(ELITE_AY);
        assertThat(ids(search().word("1.1"))).containsExactly(ELITE_AY);
    }

    @Test
    void tellsTheReleaseWithItsSoftwareArchiveHardwareAndCredits() {
        FoundRelease found = search().word("Elite AY").run().getContent().getFirst();

        assertThat(found.softwareId()).isEqualTo(ELITE);
        assertThat(found.softwareTitle()).isEqualTo("Elite");
        assertThat(found.platform()).isEqualTo("vector");
        assertThat(found.title()).isEqualTo("Elite AY");
        assertThat(found.version()).isEqualTo("1.1");
        assertThat(found.type()).isEqualTo("adaptation");
        assertThat(found.releaseDate()).isEqualTo("1987");
        assertThat(found.hardware()).containsExactlyInAnyOrder("vector06c", "floppy", "ay8910");
        assertThat(found.archiveName()).isEqualTo("elite.zip");
        assertThat(found.archiveSize()).isEqualTo(1234L);
        assertThat(found.credits()).singleElement().satisfies(credit -> {
            assertThat(credit.id()).isEqualTo(JANE);
            assertThat(credit.roles()).containsExactly("adaptation");
        });
    }

    @Test
    void narrowsToSoftwareTypesAndPlatform() {
        assertThat(ids(search().software(TETRIS))).containsExactly(TETRIS_ORIGINAL);
        assertThat(ids(search().types(ReleaseType.CRACK, ReleaseType.ORIGINAL)))
                .containsExactly(ELITE_CRACK, TETRIS_ORIGINAL);
        assertThat(ids(search().platform("bk"))).containsExactly(TETRIS_ORIGINAL);
    }

    @Test
    void narrowsToYearsLeavingOutUnknownDates() {
        assertThat(ids(search().years(1985, 1988))).containsExactly(ELITE_AY);
        assertThat(ids(search().years(1988, null))).containsExactly(ELITE_CRACK);
        assertThat(ids(search().years(null, 2000))).containsExactly(ELITE_AY, ELITE_CRACK);
    }

    @Test
    void findsHardwareAReleaseStatesOrTakesFromItsSoftwareInTheGroupsItDoesNotState() {
        assertThat(ids(search().hardware("tape"))).containsExactly(ELITE_CRACK);
        assertThat(ids(search().hardware("floppy"))).containsExactly(ELITE_AY);
        assertThat(ids(search().hardware("vector06c"))).containsExactly(ELITE_AY, ELITE_CRACK);
        assertThat(ids(search().hardware("tape", "ay8910"))).containsExactly(ELITE_AY, ELITE_CRACK);
    }

    @Test
    void findsAPersonOrAGroupCreditedByThemselvesOrUnderAnyOfTheirLabels() {
        assertThat(ids(search().credited(JANE))).containsExactly(ELITE_AY, ELITE_CRACK);
        assertThat(ids(search().credited(CREW))).containsExactly(ELITE_CRACK, TETRIS_ORIGINAL);
    }

    @Test
    void findsALabelOnlyWhereItIsCreditedItself() {
        assertThat(ids(search().credited(JD))).containsExactly(ELITE_CRACK);
        assertThat(ids(search().credited(GHOST))).containsExactly(TETRIS_ORIGINAL);
    }

    @Test
    void findsRolesOfTheCreditedOrOfAnybody() {
        assertThat(ids(search().credited(CREW).roles("developer"))).containsExactly(TETRIS_ORIGINAL);
        assertThat(ids(search().credited(JANE).roles("developer"))).isEmpty();
        assertThat(ids(search().roles("music", "release"))).containsExactly(ELITE_CRACK, TETRIS_ORIGINAL);
    }

    @Test
    void findsAFileByAPartOfTheArchiveNameOrOfAFileInsideOrByItsDigest() {
        assertThat(ids(search().fileName("elite.z"))).containsExactly(ELITE_AY);
        assertThat(ids(search().fileName("elite.rom"))).containsExactly(ELITE_AY);
        assertThat(ids(search().md5("fedcba9876543210fedcba9876543210"))).containsExactly(ELITE_AY);
        assertThat(ids(search().md5("00000000000000000000000000000000"))).isEmpty();
    }

    @Test
    void cutsTheResultIntoPagesCountingAll() {
        Page<FoundRelease> page = search().page(1, 2).run();

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).extracting(FoundRelease::id).containsExactly(TETRIS_ORIGINAL);
    }

    private Search search() {
        return new Search();
    }

    private static List<UUID> ids(Search search) {
        return search.run().getContent().stream().map(FoundRelease::id).toList();
    }

    private static UUID id(int n) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n));
    }

    /** A query built condition by condition. */
    private final class Search {

        private @Nullable CatalogKeyword word;
        private Set<SoftwareId> software = Set.of();
        private Set<ReleaseType> types = Set.of();
        private @Nullable ReleaseYear yearFrom;
        private @Nullable ReleaseYear yearTo;
        private @Nullable PlatformCode platform;
        private Set<HardwareCode> hardware = Set.of();
        private Set<SoftwareReferenceId> credited = Set.of();
        private Set<String> roles = Set.of();
        private @Nullable CatalogKeyword fileName;
        private @Nullable ReleaseFileHash md5;
        private PageRequest pageable = PageRequest.of(0, 20);

        Search word(String value) {
            word = new CatalogKeyword(value);
            return this;
        }

        Search software(UUID id) {
            software = Set.of(new SoftwareId(id));
            return this;
        }

        Search types(ReleaseType... values) {
            types = Set.of(values);
            return this;
        }

        Search years(@Nullable Integer from, @Nullable Integer to) {
            yearFrom = from == null ? null : new ReleaseYear(from);
            yearTo = to == null ? null : new ReleaseYear(to);
            return this;
        }

        Search platform(String code) {
            platform = new PlatformCode(code);
            return this;
        }

        Search hardware(String... codes) {
            hardware = Set.of(codes).stream().map(HardwareCode::new).collect(java.util.stream.Collectors.toSet());
            return this;
        }

        Search credited(UUID id) {
            credited = Set.of(new SoftwareReferenceId(id));
            return this;
        }

        Search roles(String... codes) {
            roles = Set.of(codes);
            return this;
        }

        Search fileName(String value) {
            fileName = new CatalogKeyword(value);
            return this;
        }

        Search md5(String value) {
            md5 = new ReleaseFileHash(value);
            return this;
        }

        Search page(int number, int size) {
            pageable = PageRequest.of(number, size);
            return this;
        }

        Page<FoundRelease> run() {
            return handler.handle(new SearchReleasesQuery(
                    word, software, types, yearFrom, yearTo, platform, hardware, credited, roles, fileName, md5,
                    pageable));
        }
    }
}
