package com.archive.backend.importer.app;

import static com.archive.backend.importer.infra.FakeVektorHttp.list;
import static com.archive.backend.importer.infra.FakeVektorHttp.page;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.Importer;
import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.importer.infra.FakeVektorHttp;
import com.archive.backend.software.app.DeleteSoftwareCommand;
import com.archive.backend.software.app.DeleteSoftwareHandler;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.TestActors;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The import of vektor-06c.ru against pages the site served, through the use cases of the archive; every program
 * commits for real (docs/features/vektor-import.md).
 */
@SpringBootTest
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class, ImportVektorIT.Site.class})
class ImportVektorIT {

    private static final String CATALOG = "https://vektor-06c.ru/programmnoe-obespechenie/";
    private static final String WHOLE = CATALOG + "?PAGEN_2=1";
    private static final String OS = CATALOG + "operatsionnye-sistemy/";

    private static final String JOE_BLADE = "/programmnoe-obespechenie/arkadnye-igry/joe-blade-/";
    private static final String MICRODOS =
            "/programmnoe-obespechenie/operatsionnye-sistemy/mikrodos-3-1-bios-3-11-f-11/";
    private static final String CPM = "/programmnoe-obespechenie/operatsionnye-sistemy/cpm-59/";
    private static final String BOLDBIOS = "/programmnoe-obespechenie/operatsionnye-sistemy/boldbios/";
    private static final String CPM_ARCHIVE_PAGE = "https://vektor-06c.ru" + CPM;

    private static final UUID ARCADE = UUID.fromString("8536e450-1fc7-5985-9cd2-97b499713a67");

    @TestConfiguration(proxyBeanMethods = false)
    static class Site {
        @Bean
        @Primary
        FakeVektorHttp fakeVektorHttp() {
            return new FakeVektorHttp();
        }
    }

    @Autowired
    private ImportVektorHandler handler;

    @Autowired
    private FakeVektorHttp http;

    @Autowired
    private DeleteSoftwareHandler deleteSoftware;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM software WHERE added_by = ?", Importer.ID.value());
        jdbc.update("DELETE FROM label WHERE id IN (SELECT entity_id FROM external_origin WHERE kind = 'LABEL')");
        jdbc.update("DELETE FROM external_origin");
        http.clear();
    }

    @Test
    void addsAProgramAsSoftwareWithItsOneRelease() {
        servePrograms();
        http.serve(WHOLE, list("892", JOE_BLADE));

        assertThat(handler.handle(new ImportVektorCommand(null))).isEqualTo(new ImportSummary(1, 0, 0, 0));

        Map<String, Object> software = jdbc.queryForMap(
                "SELECT id, description, release_year, platform_code, added_by FROM software WHERE title = 'Joe Blade'");
        UUID id = (UUID) software.get("id");
        assertThat((String) software.get("description")).startsWith("Игроку предстоит управлять персонажем");
        assertThat(software.get("release_year")).isEqualTo(1987);
        assertThat(software.get("platform_code")).isEqualTo("vector");
        assertThat(software.get("added_by")).isEqualTo(Importer.ID.value());
        assertThat(jdbc.queryForList(
                        "SELECT hardware_code FROM software_hardware WHERE software_id = ?", String.class, id))
                .containsExactly("vector06c");
        assertThat(jdbc.queryForList("SELECT category_id FROM software_category WHERE software_id = ?", UUID.class, id))
                .containsExactly(ARCADE);

        Map<String, Object> release = jdbc.queryForMap(
                "SELECT type, release_year, archive_file_name, added_by FROM software_release WHERE software_id = ?",
                id);
        assertThat(release.get("type")).isEqualTo("original");
        assertThat(release.get("release_year")).isEqualTo(2026);
        assertThat(release.get("archive_file_name")).isNotNull();
        assertThat(release.get("added_by")).isEqualTo(Importer.ID.value());

        assertThat(jdbc.queryForObject(
                        "SELECT format FROM software_screenshot WHERE software_id = ?", String.class, id))
                .isEqualTo("jpeg");
        assertThat(jdbc.queryForObject(
                        "SELECT entity_id FROM external_origin"
                                + " WHERE origin = 'vektor-06c.ru' AND kind = 'SOFTWARE' AND external_id = '892'",
                        UUID.class))
                .isEqualTo(id);
        assertThat(jdbc.queryForObject(
                        "SELECT url FROM external_origin WHERE kind = 'SOFTWARE' AND external_id = '892'",
                        String.class))
                .isEqualTo("https://vektor-06c.ru" + JOE_BLADE);
    }

    @Test
    void creditsEachAuthorAsALabelOfNobodyWithAnUnknownRole() {
        servePrograms();
        http.serve(WHOLE, list("754", MICRODOS, "892", JOE_BLADE));

        handler.handle(new ImportVektorCommand(null));

        assertThat(credits("МикроДОС 3.1 (BIOS 3.11.F.11)")).containsExactly("Филиппов Е.В.:{unknown}");
        assertThat(credits("Joe Blade")).containsExactly("Colin Swinbourne / Никита Зимин:{unknown}");
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM label l JOIN external_origin o ON o.entity_id = l.id"
                                + " WHERE o.kind = 'LABEL' AND l.person_id IS NULL AND l.group_id IS NULL",
                        Integer.class))
                .isEqualTo(2);
    }

    @Test
    void tellsTheStorageByTheFilesOfTheArchive() {
        servePrograms();
        http.serve(OS, list("753", CPM, "763", BOLDBIOS));

        handler.handle(new ImportVektorCommand(new VektorSection("operatsionnye-sistemy")));

        assertThat(releaseHardware("CPM-59")).containsExactly("tape");
        assertThat(releaseHardware("BoldBIOS")).isEmpty();
    }

    @Test
    void leavesOutThePictureThatSaysThereIsNoScreenshot() {
        servePrograms();
        http.serve(OS, list("763", BOLDBIOS));

        handler.handle(new ImportVektorCommand(new VektorSection("operatsionnye-sistemy")));

        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software_screenshot sc JOIN software s ON s.id = sc.software_id"
                                + " WHERE s.added_by = ?",
                        Integer.class,
                        Importer.ID.value()))
                .isZero();
    }

    @Test
    void takesEachProgramOnceHoweverOftenItRuns() {
        servePrograms();
        http.serve(OS, list("754", MICRODOS, "753", CPM));
        ImportVektorCommand command = new ImportVektorCommand(new VektorSection("operatsionnye-sistemy"));

        assertThat(handler.handle(command)).isEqualTo(new ImportSummary(2, 0, 0, 0));
        assertThat(handler.handle(command)).isEqualTo(new ImportSummary(0, 2, 0, 0));

        assertThat(importedSoftware()).isEqualTo(2);
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software_release r JOIN software s ON s.id = r.software_id"
                                + " WHERE s.added_by = ?",
                        Integer.class,
                        Importer.ID.value()))
                .isEqualTo(2);
    }

    @Test
    void addsWhatIsNewAndKeepsWhatTheSiteDeleted() {
        servePrograms();
        ImportVektorCommand command = new ImportVektorCommand(new VektorSection("operatsionnye-sistemy"));
        http.serve(OS, list("754", MICRODOS));
        handler.handle(command);

        http.serve(OS, list("753", CPM));

        assertThat(handler.handle(command)).isEqualTo(new ImportSummary(1, 0, 0, 0));
        assertThat(importedSoftware()).isEqualTo(2);
    }

    @Test
    void takesAgainAProgramWhoseSoftwareWasDeleted() {
        servePrograms();
        ImportVektorCommand command = new ImportVektorCommand(new VektorSection("operatsionnye-sistemy"));
        http.serve(OS, list("753", CPM));
        handler.handle(command);
        UUID deleted = jdbc.queryForObject(
                "SELECT entity_id FROM external_origin WHERE kind = 'SOFTWARE' AND external_id = '753'", UUID.class);

        deleteSoftware.handle(new DeleteSoftwareCommand(TestActors.ADMIN, new SoftwareId(deleted)));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM external_origin WHERE kind = 'SOFTWARE'", Integer.class))
                .isZero();
        assertThat(handler.handle(command)).isEqualTo(new ImportSummary(1, 0, 0, 0));
        assertThat(importedSoftware()).isEqualTo(1);
    }

    @Test
    void createsALabelSharedByProgramsOnce() {
        servePrograms();
        http.serve(
                "https://vektor-06c.ru/programmnoe-obespechenie/operatsionnye-sistemy/cpm-59-copy/",
                page("program-753.html"));
        http.serve(OS, list("753", CPM, "9753", "/programmnoe-obespechenie/operatsionnye-sistemy/cpm-59-copy/"));

        handler.handle(new ImportVektorCommand(new VektorSection("operatsionnye-sistemy")));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM label WHERE name = 'Графов А.'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software_contribution c JOIN label l ON l.id = c.label_id"
                                + " WHERE l.name = 'Графов А.'",
                        Integer.class))
                .isEqualTo(2);
    }

    @Test
    void leavesOutTheUtilitiesForThePc() {
        String utility = "/programmnoe-obespechenie/utility-dlya-pc/512-demo/";
        http.serve("https://vektor-06c.ru" + utility, page("program-524.html"));
        http.serve(WHOLE, list("524", utility));

        assertThat(handler.handle(new ImportVektorCommand(null))).isEqualTo(new ImportSummary(0, 0, 1, 0));
        assertThat(importedSoftware()).isZero();
    }

    @Test
    void leavesOutAProgramThatFailsWithoutATraceAndGoesOn() {
        servePrograms();
        http.fail("https://vektor-06c.ru/upload/iblock/archive-of-cpm.zip");
        http.serve(
                CPM_ARCHIVE_PAGE,
                page("program-753.html")
                        .replaceAll("href=\"(/upload/[^\"]+\\.zip)\"", "href=\"/upload/iblock/archive-of-cpm.zip\""));
        http.serve(OS, list("753", CPM, "754", MICRODOS));

        assertThat(handler.handle(new ImportVektorCommand(new VektorSection("operatsionnye-sistemy"))))
                .isEqualTo(new ImportSummary(1, 0, 0, 1));

        assertThat(jdbc.queryForList(
                        "SELECT title FROM software WHERE added_by = ?", String.class, Importer.ID.value()))
                .containsExactly("МикроДОС 3.1 (BIOS 3.11.F.11)");
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM label WHERE name IN ('Графов А.', 'Синицын А.')", Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM external_origin WHERE external_id IN ('753', 'Графов А.')",
                        Integer.class))
                .isZero();
    }

    private void servePrograms() {
        http.serve("https://vektor-06c.ru" + JOE_BLADE, page("program-892.html"));
        http.serve("https://vektor-06c.ru" + MICRODOS, page("program-754.html"));
        http.serve("https://vektor-06c.ru" + CPM, page("program-753.html"));
        http.serve("https://vektor-06c.ru" + BOLDBIOS, page("program-763.html"));
    }

    private int importedSoftware() {
        Integer count = jdbc.queryForObject(
                "SELECT count(*) FROM software WHERE added_by = ?", Integer.class, Importer.ID.value());
        return count == null ? 0 : count;
    }

    private List<String> credits(String title) {
        return jdbc.queryForList(
                "SELECT l.name || ':' || c.role_codes::text FROM software_contribution c"
                        + " JOIN label l ON l.id = c.label_id JOIN software s ON s.id = c.software_id"
                        + " WHERE s.title = ? ORDER BY l.name",
                String.class,
                title);
    }

    private List<String> releaseHardware(String title) {
        return jdbc.queryForList(
                "SELECT h.hardware_code FROM software_release_hardware h"
                        + " JOIN software_release r ON r.id = h.release_id JOIN software s ON s.id = r.software_id"
                        + " WHERE s.title = ?",
                String.class,
                title);
    }
}
