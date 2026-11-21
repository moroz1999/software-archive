package com.archive.backend.importer.app;

import static com.archive.backend.importer.infra.FakeSourceHttp.basisPage;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.Importer;
import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import com.archive.backend.importer.domain.run.ImportScope;
import com.archive.backend.importer.infra.FakeSourceHttp;
import com.archive.backend.importer.read.ImportRunReadRepository;
import com.archive.backend.importer.read.ImportRunReport;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The import of the card index "Базис" against pages the site served, through the use cases of the archive; every
 * program commits for real (docs/features/basis-import.md). The list is the real one cut down to five cards.
 */
@SpringBootTest
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class, ImportBasisIT.Site.class})
class ImportBasisIT {

    private static final String SCALAR = "https://caglrc.cc/scalar/";
    private static final String LIST = SCALAR + "categories/all/";
    private static final UUID GAMES = UUID.fromString("081bf3a1-782b-5a8b-8d12-3b155e0a58cb");
    private static final UUID UNKNOWN_COUNTRY = UUID.fromString("00000000-0000-0000-0000-00000000c001");
    private static final UUID JAPAN = UUID.fromString("00000000-0000-0000-00b5-000000000001");

    @TestConfiguration(proxyBeanMethods = false)
    static class Site {
        @Bean
        @Primary
        FakeSourceHttp fakeSourceHttp() {
            return new FakeSourceHttp();
        }
    }

    @Autowired
    private ImportBasisHandler handler;

    @Autowired
    private FakeSourceHttp http;

    @Autowired
    private ImportRunRepository runs;

    @Autowired
    private ImportRunReadRepository reports;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void serveTheSite() {
        cleanUp();
        jdbc.update("INSERT INTO country (id) VALUES (?)", JAPAN);
        jdbc.update(
                "INSERT INTO country_name (country_id, language, name) VALUES (?, 'en', 'Japan'), (?, 'ru', 'Япония')",
                JAPAN,
                JAPAN);
        http.serve(LIST, basisPage("list-few.html"));
        for (String ware : List.of("30", "231", "388", "418", "558")) {
            http.serve(SCALAR + "ware/" + ware + "/", basisPage("ware-" + ware + ".html"));
        }
        for (String author : List.of("5", "20", "21", "3", "215")) {
            http.serve(SCALAR + "author/" + author + "/", basisPage("author-" + author + ".html"));
        }
    }

    @AfterEach
    void cleanUp() {
        UUID importer = Importer.ID.value();
        jdbc.update("DELETE FROM software WHERE added_by = ?", importer);
        jdbc.update(
                "DELETE FROM entity_location WHERE entity_id IN (SELECT id FROM label WHERE added_by = ?)", importer);
        jdbc.update("DELETE FROM label WHERE added_by = ?", importer);
        jdbc.update(
                "DELETE FROM entity_location WHERE entity_id IN (SELECT id FROM person WHERE added_by = ?)"
                        + " OR entity_id IN (SELECT id FROM \"group\" WHERE added_by = ?)",
                importer,
                importer);
        jdbc.update("DELETE FROM person WHERE added_by = ?", importer);
        jdbc.update("DELETE FROM \"group\" WHERE added_by = ?", importer);
        jdbc.update("DELETE FROM external_origin");
        jdbc.update("DELETE FROM entity_location WHERE country_id = ?", JAPAN);
        jdbc.update("DELETE FROM city WHERE country_id = ?", UNKNOWN_COUNTRY);
        jdbc.update("DELETE FROM country WHERE id = ?", JAPAN);
        jdbc.update("DELETE FROM import_run");
        http.clear();
    }

    @Test
    void addsEveryProgramOfTheListAsSoftwareWithItsOneReleaseScreenshotsAndOrigin() {
        assertThat(run()).isEqualTo(new ImportSummary(5, 0, 0, 0));

        Map<String, Object> zoneX = jdbc.queryForMap(
                "SELECT id, description, release_year, preservation FROM software WHERE title = 'Zone X'");
        UUID id = (UUID) Objects.requireNonNull(zoneX.get("id"));
        assertThat(zoneX.get("description")).isEqualTo("Вертикальный скроллер-шутер.");
        assertThat(zoneX.get("release_year")).isEqualTo(1994);
        assertThat(zoneX.get("preservation")).isEqualTo("unknown");
        assertThat(hardware(id)).containsExactly("vector06c");
        assertThat(jdbc.queryForList("SELECT category_id FROM software_category WHERE software_id = ?", UUID.class, id))
                .containsExactly(GAMES);
        assertThat(jdbc.queryForObject(
                        "SELECT archive_file_name FROM software_release WHERE software_id = ?", String.class, id))
                .isEqualTo("xzone.com");
        assertThat(jdbc.queryForList(
                        "SELECT file_name FROM software_screenshot WHERE software_id = ? ORDER BY position",
                        String.class,
                        id))
                .containsExactly("xzone.png");
        assertThat(jdbc.queryForObject(
                        "SELECT url FROM external_origin WHERE origin = 'caglrc.cc' AND kind = 'SOFTWARE'"
                                + " AND external_id = '30'",
                        String.class))
                .isEqualTo(SCALAR + "ware/30/");
    }

    @Test
    void takesTheComputerAndThePreservationStatusFromTheCategories() {
        run();

        assertThat(hardware(software("Посадка"))).containsExactly("krista2");
        assertThat(jdbc.queryForObject("SELECT preservation FROM software WHERE title = 'Nature'", String.class))
                .isEqualTo("missing-in-action");
        assertThat(jdbc.queryForObject(
                        "SELECT archive_file_name FROM software_release WHERE software_id = ?",
                        String.class,
                        software("Nature")))
                .isNull();
    }

    @Test
    void creditsPeopleUnderTheirNicknamesAndAuthorsWithoutANicknameAsLabelsOfNobody() {
        run();

        assertThat(jdbc.queryForObject(
                        "SELECT p.real_name FROM person p JOIN label l ON l.person_id = p.id WHERE l.name = 'S.E.S.'",
                        String.class))
                .isEqualTo("Ермолаев Сергей");
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software_contribution c JOIN label l ON l.id = c.label_id"
                                + " WHERE l.name = 'S.E.S.'",
                        Integer.class))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject(
                        "SELECT p.real_name FROM person p JOIN label l ON l.person_id = p.id WHERE l.name = 'ivagor'",
                        String.class))
                .isEqualTo("Городецкий И.");
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM label WHERE name IN ('JALECO', 'Желнов П.А.')"
                                + " AND person_id IS NULL AND group_id IS NULL",
                        Integer.class))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software_contribution WHERE software_id = ?",
                        Integer.class,
                        software("Посадка")))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM label WHERE name = 'Неизвестный Автор'", Integer.class))
                .isZero();
    }

    @Test
    void placesAPersonInTheCityOfTheirPage() {
        run();

        assertThat(jdbc.queryForObject(
                        "SELECT n.name FROM entity_location el JOIN city_name n ON n.city_id = el.city_id"
                                + " JOIN label l ON l.person_id = el.entity_id"
                                + " WHERE l.name = 'S.E.S.' AND n.language = 'ru'",
                        String.class))
                .isEqualTo("Санкт-Петербург");
    }

    @Test
    void placesAnAuthorInTheCountryTheirPageNamesAndElseInACityOfThatName() {
        run();

        assertThat(jdbc.queryForObject(
                        "SELECT el.country_id FROM entity_location el JOIN label l ON l.id = el.entity_id"
                                + " WHERE l.name = 'JALECO'",
                        UUID.class))
                .isEqualTo(JAPAN);
        assertThat(jdbc.queryForObject(
                        "SELECT n.name FROM entity_location el JOIN city_name n ON n.city_id = el.city_id"
                                + " JOIN label l ON l.id = el.entity_id WHERE l.name = 'Желнов П.А.'",
                        String.class))
                .isEqualTo("г.Отрадный, Самарская область");
    }

    @Test
    void readsThePageOfAnAuthorOnceForAllTheirPrograms() {
        run();

        assertThat(http.asked()).filteredOn((SCALAR + "author/5/")::equals).hasSize(1);
    }

    @Test
    void knowsEveryProgramOnAnotherRun() {
        run();

        assertThat(run()).isEqualTo(new ImportSummary(0, 5, 0, 0));
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software WHERE added_by = ?", Integer.class, Importer.ID.value()))
                .isEqualTo(5);
    }

    @Test
    void leavesOutSoftwareForThePc() {
        http.serve(
                SCALAR + "ware/30/",
                basisPage("ware-30.html").replace("/scalar/categories/game/\">game<", "/scalar/categories/pc/\">pc<"));

        assertThat(run()).isEqualTo(new ImportSummary(4, 0, 1, 0));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software WHERE title = 'Zone X'", Integer.class))
                .isZero();
        assertThat(reports.findLatest().orElseThrow().skippedPrograms())
                .singleElement()
                .satisfies(skipped -> {
                    assertThat(skipped.externalId()).isEqualTo("30");
                    assertThat(skipped.url()).isEqualTo(SCALAR + "ware/30/");
                    assertThat(skipped.title()).isEqualTo("Zone X");
                    assertThat(skipped.reason()).isEqualTo("category");
                    assertThat(skipped.detail()).isEqualTo("pc");
                });
    }

    @Test
    void endsAtTheLastProgramOfTheListItCounted() {
        run();

        ImportRunReport report = reports.findLatest().orElseThrow();
        assertThat(report.progress().position()).isEqualTo(5);
        assertThat(report.progress().total()).isEqualTo(5);
        assertThat(report.progress().url()).startsWith(SCALAR + "ware/");
    }

    @Test
    void goesOnPastAProgramThatFails() {
        http.fail(SCALAR + "ware/418/");

        assertThat(run()).isEqualTo(new ImportSummary(4, 0, 0, 1));
    }

    private ImportSummary run() {
        ImportRunId id = new ImportRunId(UUID.randomUUID());
        runs.save(ImportRun.start(id, new ImportScope("caglrc.cc", null), Instant.now()));
        return handler.handle(new ImportBasisCommand(id));
    }

    private UUID software(String title) {
        return Objects.requireNonNull(jdbc.queryForObject(
                "SELECT id FROM software WHERE title = ? AND added_by = ?", UUID.class, title, Importer.ID.value()));
    }

    private List<String> hardware(UUID software) {
        return jdbc.queryForList(
                "SELECT hardware_code FROM software_hardware WHERE software_id = ?", String.class, software);
    }
}
