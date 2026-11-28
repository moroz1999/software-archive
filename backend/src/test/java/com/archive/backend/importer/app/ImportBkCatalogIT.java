package com.archive.backend.importer.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.Importer;
import com.archive.backend.importer.domain.bkcatalog.BkCatalogSection;
import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import com.archive.backend.importer.domain.run.ImportScope;
import com.archive.backend.importer.infra.FakeSourceHttp;
import com.archive.backend.importer.read.ImportRunListReadEntry;
import com.archive.backend.importer.read.ImportRunLogLine;
import com.archive.backend.importer.read.ImportRunReadRepository;
import com.archive.backend.importer.read.ImportRunReport;
import com.archive.backend.importer.read.ImportRunSkippedEntry;
import com.archive.backend.importer.read.ImportRunWarningEntry;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The import of bk-catalog against lists and files its repository served, through the use cases of the archive; every
 * entry commits for real (docs/features/bk-catalog-import.md). It shares the context of the import of "Базис": the
 * same fake source answers both.
 */
@SpringBootTest
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class, ImportBasisIT.Site.class})
class ImportBkCatalogIT {

    private static final String FILES = "https://raw.githubusercontent.com/kalininskiy/bk-catalog/main/";
    private static final String HOME = "https://kalininskiy.github.io/bk-catalog/";
    private static final String HEADER = "ID,Название,Авторы,Издатель,Год выпуска,Платформа,Жанр,Описание,"
            + "Имя файла 1,Описание файла 1,Скриншот 1,Ссылка на страницу\n";
    private static final UUID ADULT = UUID.fromString("c3d2848f-3803-5b6b-b52d-102ebd3fa715");
    private static final UUID WILD = UUID.fromString("64ad90af-cf42-59c0-ad1f-52500c3ec926");
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0};

    @Autowired
    private ImportBkCatalogHandler handler;

    @Autowired
    private FakeSourceHttp http;

    @Autowired
    private ImportRunRepository runs;

    @Autowired
    private ImportRunReadRepository reports;

    @Autowired
    private JdbcTemplate jdbc;

    /** When the last run of the test started: each a second after the one before, so the latest is the one started last. */
    private Instant started = Instant.now();

    @BeforeEach
    void serveTheRepository() {
        cleanUp();
        http.serve(
                FILES + "content/games.csv",
                HEADER
                        + "555893,Digger,Мачуговский А. (Manwe/SandS),АКТ,1990,БК0010,\"Аркада, Лабиринт\",Копай.,"
                        + "digger.zip,,digger.png,https://r-games.net/digger.html\n"
                        + "555777,Kama,,,1993,БК0010,Эротическая,,kama.zip,,,\n");
        http.serve(
                FILES + "content/software.csv",
                HEADER
                        + "2026321,BK Turbo 8,gid,,2020,Windows,Ассемблер,Кросс-ассемблер.,bkturbo8.zip,,,\n"
                        + "2026342,БЕЙСИК 85,,,1985,,Язык программирования,,basic01bk.zip,,,\n");
        http.serve(
                FILES + "content/demoscene.csv",
                HEADER
                        + "3000215,Bad Apple,,Excess Team,2021,БК0011М,wild,Шикарная демка.,bad_apple_bk.zip,,lost.png,\n");
        http.serve(FILES + "bk_games_files/digger.zip", FakeSourceHttp.zip("DIGGER.BIN"));
        http.serve(FILES + "bk_games_files/kama.zip", FakeSourceHttp.zip("KAMA.BIN"));
        http.serve(FILES + "bk_games_screenshots/digger.png", PNG);
        http.serve(FILES + "bk_files/basic01bk.zip", FakeSourceHttp.zip("BASIC.BIN"));
        http.serve(FILES + "bk_files/bad_apple_bk.zip", FakeSourceHttp.zip("APPLE.BIN"));
    }

    @AfterEach
    void cleanUp() {
        UUID importer = Importer.ID.value();
        jdbc.update("DELETE FROM software WHERE added_by = ?", importer);
        jdbc.update("DELETE FROM label WHERE added_by = ?", importer);
        jdbc.update("DELETE FROM external_origin");
        jdbc.update("DELETE FROM import_run");
        http.clear();
    }

    @Test
    void importsEverySectionOfTheCatalogAndLeavesOutSoftwareForThePc() {
        assertThat(run(null)).isEqualTo(new ImportSummary(4, 0, 1, 0));

        assertThat(jdbc.queryForList(
                        "SELECT title FROM software WHERE added_by = ? ORDER BY title",
                        String.class,
                        Importer.ID.value()))
                .containsExactly("Bad Apple", "Digger", "Kama", "БЕЙСИК 85");
        assertThat(entries(lastReport(), ImportRunListReadEntry.class))
                .containsExactly(
                        new ImportRunListReadEntry(FILES + "content/games.csv", 1, 3, 2),
                        new ImportRunListReadEntry(FILES + "content/software.csv", 2, 3, 2),
                        new ImportRunListReadEntry(FILES + "content/demoscene.csv", 3, 3, 1));
        assertThat(entries(lastReport(), ImportRunSkippedEntry.class))
                .containsExactly(new ImportRunSkippedEntry(
                        "software-2026321", HOME + "#software-2026321", "BK Turbo 8", "platform", "Windows"));
    }

    @Test
    void importsOnlyTheSectionChosen() {
        assertThat(run(BkCatalogSection.DEMOSCENE)).isEqualTo(new ImportSummary(1, 0, 0, 0));

        assertThat(http.asked()).contains(FILES + "content/demoscene.csv").doesNotContain(FILES + "content/games.csv");
        assertThat(entries(lastReport(), ImportRunListReadEntry.class))
                .containsExactly(new ImportRunListReadEntry(FILES + "content/demoscene.csv", 1, 1, 1));
    }

    @Test
    void addsAGameWithItsReleaseScreenshotAuthorsCategoriesLinkAndOrigin() {
        run(BkCatalogSection.GAMES);

        UUID digger = software("Digger");
        assertThat(jdbc.queryForMap("SELECT description, release_year FROM software WHERE id = ?", digger))
                .containsEntry("description", "Копай.")
                .containsEntry("release_year", 1990);
        assertThat(jdbc.queryForList(
                        "SELECT hardware_code FROM software_hardware WHERE software_id = ?", String.class, digger))
                .containsExactly("bk0010");
        assertThat(jdbc.queryForList(
                        "SELECT c.title FROM software_category sc JOIN category_title c ON c.category_id = sc.category_id"
                                + " WHERE sc.software_id = ? AND c.language = 'en' ORDER BY c.title",
                        String.class,
                        digger))
                .containsExactly("Arcade", "Maze");
        assertThat(jdbc.queryForObject(
                        "SELECT archive_file_name FROM software_release WHERE software_id = ?", String.class, digger))
                .isEqualTo("digger.zip");
        assertThat(jdbc.queryForList(
                        "SELECT file_name FROM software_screenshot WHERE software_id = ?", String.class, digger))
                .containsExactly("digger.png");
        assertThat(jdbc.queryForList(
                        "SELECT l.name || ': ' || array_to_string(c.role_codes, ' ') FROM software_contribution c"
                                + " JOIN label l ON l.id = c.label_id WHERE c.software_id = ?",
                        String.class,
                        digger))
                .containsExactlyInAnyOrder("Мачуговский А. (Manwe/SandS): unknown", "АКТ: publisher");
        assertThat(jdbc.queryForList(
                        "SELECT type || ' ' || url FROM software_links WHERE software_id = ?", String.class, digger))
                .containsExactly("general https://r-games.net/digger.html");
        assertThat(jdbc.queryForObject(
                        "SELECT url FROM external_origin WHERE origin = 'bk-catalog' AND kind = 'SOFTWARE'"
                                + " AND external_id = 'game-555893'",
                        String.class))
                .isEqualTo(HOME + "#game-555893");
    }

    @Test
    void placesAnEroticGameAmongAdultGamesAndAWildDemoUnderDemo() {
        run(null);

        assertThat(categories(software("Kama"))).containsExactly(ADULT);
        assertThat(categories(software("Bad Apple"))).containsExactly(WILD);
        assertThat(jdbc.queryForObject(
                        "SELECT t.title FROM category c JOIN category_title t ON t.category_id = c.parent_id"
                                + " WHERE c.id = ? AND t.language = 'en'",
                        String.class,
                        WILD))
                .isEqualTo("Demo");
    }

    @Test
    void warnsOfAScreenshotTheRepositoryLacksAndCountsIt() {
        run(BkCatalogSection.DEMOSCENE);

        ImportRunReport report = lastReport();
        assertThat(report.warnings()).isOne();
        assertThat(entries(report, ImportRunWarningEntry.class))
                .containsExactly(new ImportRunWarningEntry(
                        "demo-3000215", HOME + "#demo-3000215", "Bad Apple", "screenshot-missing", "lost.png"));
        assertThat(software("Bad Apple")).isNotNull();
    }

    @Test
    void knowsEveryEntryOnAnotherRun() {
        run(null);

        assertThat(run(null)).isEqualTo(new ImportSummary(0, 4, 1, 0));
    }

    @Test
    void goesOnPastAnEntryThatFails() {
        http.fail(FILES + "bk_games_files/digger.zip");

        assertThat(run(BkCatalogSection.GAMES)).isEqualTo(new ImportSummary(1, 0, 0, 1));
        assertThat(lastReport().state()).isEqualTo("DONE");
    }

    private ImportSummary run(@Nullable BkCatalogSection section) {
        ImportRunId id = new ImportRunId(UUID.randomUUID());
        started = started.plusSeconds(1);
        runs.save(ImportRun.start(id, new ImportScope("bk-catalog", section), started));
        return handler.handle(new ImportBkCatalogCommand(id, section));
    }

    private ImportRunReport lastReport() {
        return reports.findLatest(null).orElseThrow();
    }

    private static <T> List<T> entries(ImportRunReport report, Class<T> kind) {
        return report.log().stream()
                .map(ImportRunLogLine::entry)
                .filter(kind::isInstance)
                .map(kind::cast)
                .toList();
    }

    private UUID software(String title) {
        return Objects.requireNonNull(jdbc.queryForObject(
                "SELECT id FROM software WHERE title = ? AND added_by = ?", UUID.class, title, Importer.ID.value()));
    }

    private List<UUID> categories(UUID software) {
        return jdbc.queryForList(
                "SELECT category_id FROM software_category WHERE software_id = ?", UUID.class, software);
    }
}
