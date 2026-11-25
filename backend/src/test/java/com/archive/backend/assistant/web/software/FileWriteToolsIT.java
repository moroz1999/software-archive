package com.archive.backend.assistant.web.software;

import static com.archive.backend.assistant.web.tool.WriteToolsTestData.JD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.IntegrationTest;
import com.archive.backend.assistant.web.tool.ToolArgumentsException;
import com.archive.backend.assistant.web.tool.ToolCalls;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolJson;
import com.archive.backend.assistant.web.tool.WriteToolsTestData;
import com.archive.backend.remote.TestArchives;
import com.archive.backend.remote.TestImages;
import com.archive.backend.remote.TestSite;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.BusinessException;
import com.archive.backend.user.TestActors;
import com.jayway.jsonpath.DocumentContext;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/** The files a plan adds from other sites and deletes: screenshots and release archives, by the rules of uploads. */
@IntegrationTest
@Transactional
@Rollback
class FileWriteToolsIT {

    private static final ToolContext MEMBER = new ToolContext(TestActors.MEMBER, Language.EN);
    private static final ToolContext OTHER = new ToolContext(TestActors.OTHER, Language.EN);

    @Autowired
    private ToolCalls calls;

    @Autowired
    private JdbcTemplate jdbc;

    private final TestSite site = TestSite.start();
    private UUID elite;
    private UUID ay;

    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        WriteToolsTestData.insert(jdbc);
        site.serve("/one.png", 200, Map.of("Content-Type", "image/png"), TestImages.png(8, 6))
                .serve("/two.png", 200, Map.of("Content-Type", "image/png"), TestImages.png(6, 8))
                .serve("/page.html", 200, Map.of("Content-Type", "text/html"), "<p>".getBytes(StandardCharsets.UTF_8))
                .serve(
                        "/download",
                        200,
                        Map.of("Content-Disposition", "attachment; filename=\"elite-ay.zip\""),
                        TestArchives.zip("ELITE.BAS"));
        elite = calls.write("create_software", "{\"title\": \"Elite\", \"platform\": \"vector\"}", MEMBER)
                .uuid();
        ay = calls.write("create_release", """
                        {"softwareId": "%s", "type": "adaptation", "version": "1.1",
                         "credits": [{"id": "%s", "type": "author-label", "roles": ["adaptation"]}]}
                        """.formatted(elite, JD), MEMBER).uuid();
    }

    @AfterEach
    void stop() {
        site.close();
    }

    @Test
    void addsScreenshotsOfSoftwareFromTheirAddresses() {
        String add = screenshots(null, "/one.png", "/two.png");
        assertThat(ToolJson.list(ToolJson.of(calls.state("add_screenshots", add, MEMBER)), "$"))
                .isEmpty();

        UUID written = calls.write("add_screenshots", add, MEMBER).uuid();

        assertThat(written).isEqualTo(elite);
        assertThat(ToolJson.list(software(), "$.screenshots")).hasSize(2);
        assertThat(ToolJson.list(ToolJson.of(calls.state("add_screenshots", add, MEMBER)), "$"))
                .hasSize(2);
    }

    @Test
    void addsScreenshotsOfARelease() {
        calls.write("add_screenshots", screenshots(ay, "/one.png"), MEMBER);

        assertThat(ToolJson.list(release(), "$.screenshots")).hasSize(1);
        assertThat(ToolJson.list(software(), "$.screenshots")).isEmpty();
    }

    @Test
    void addsPicturesToAGalleryOfTheSoftwareApartFromItsScreenshots() {
        String add = gallery("maps", null, "/one.png");
        assertThat(ToolJson.list(ToolJson.of(calls.state("add_screenshots", add, MEMBER)), "$"))
                .isEmpty();

        calls.write("add_screenshots", add, MEMBER);
        calls.write("add_screenshots", gallery("covers", null, "/two.png"), MEMBER);

        DocumentContext software = software();
        assertThat(ToolJson.list(software, "$.maps")).hasSize(1);
        assertThat(ToolJson.list(software, "$.covers")).hasSize(1);
        assertThat(ToolJson.list(software, "$.screenshots")).isEmpty();
    }

    @Test
    void givesAReleaseScreenshotsOnly() {
        assertThatThrownBy(() -> calls.write("add_screenshots", gallery("adverts", ay, "/one.png"), MEMBER))
                .isInstanceOf(ToolArgumentsException.class);

        assertThat(ToolJson.list(release(), "$.screenshots")).isEmpty();
    }

    @Test
    void deletesAPictureOfAGallery() {
        calls.write("add_screenshots", gallery("adverts", null, "/one.png", "/two.png"), MEMBER);
        String first = software().read("$.adverts[0].id", String.class);

        calls.write("delete_screenshot", """
                {"softwareId": "%s", "screenshotId": "%s", "gallery": "adverts"}
                """.formatted(elite, first), MEMBER);

        assertThat(ToolJson.list(software(), "$.adverts[*].id")).hasSize(1).doesNotContain(first);
    }

    @Test
    void addsNoneOfTheScreenshotsWhenOneIsNoPicture() {
        assertThatThrownBy(() -> calls.write("add_screenshots", screenshots(null, "/one.png", "/page.html"), MEMBER))
                .isInstanceOf(BusinessException.class);

        assertThat(ToolJson.list(software(), "$.screenshots")).isEmpty();
    }

    @Test
    void givesAReleaseItsArchiveUnderTheNameTheSiteGivesKeepingTheRest() {
        String set = archive(null);
        DocumentContext state = ToolJson.of(calls.state("set_release_archive", set, MEMBER));

        UUID written = calls.write("set_release_archive", set, MEMBER).uuid();

        assertThat(state.read("$.release.archive", Object.class)).isNull();
        assertThat(written).isEqualTo(ay);
        DocumentContext release = release();
        assertThat(release.read("$.release.archive.fileName", String.class)).isEqualTo("elite-ay.zip");
        assertThat(release.read("$.release.version", String.class)).isEqualTo("1.1");
        assertThat(release.read("$.release.type", String.class)).isEqualTo("adaptation");
        assertThat(ToolJson.list(release, "$.release.credits[*].id")).containsExactly(JD.toString());
    }

    @Test
    void givesTheArchiveTheNameAPlanGivesIt() {
        calls.write("set_release_archive", archive("Elite AY.zip"), MEMBER);

        assertThat(release().read("$.release.archive.fileName", String.class)).isEqualTo("Elite AY.zip");
    }

    @Test
    void deletesAScreenshotShowingIt() {
        calls.write("add_screenshots", screenshots(null, "/one.png", "/two.png"), MEMBER);
        String first = software().read("$.screenshots[0].id", String.class);
        String delete = "{\"softwareId\": \"%s\", \"screenshotId\": \"%s\"}".formatted(elite, first);

        DocumentContext state = ToolJson.of(calls.state("delete_screenshot", delete, MEMBER));
        calls.write("delete_screenshot", delete, MEMBER);

        assertThat(state.read("$.id", String.class)).isEqualTo(first);
        assertThat(ToolJson.list(software(), "$.screenshots[*].id")).hasSize(1).doesNotContain(first);
        assertThatThrownBy(() -> calls.state("delete_screenshot", delete, MEMBER))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void leavesWhoMayChangeTheFilesToTheArchive() {
        assertThatThrownBy(() -> calls.write("add_screenshots", screenshots(null, "/one.png"), OTHER))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> calls.write("set_release_archive", archive(null), OTHER))
                .isInstanceOf(BusinessException.class);
    }

    private DocumentContext software() {
        return ToolJson.of(calls.read("get_software", "{\"id\": \"%s\"}".formatted(elite), MEMBER));
    }

    private DocumentContext release() {
        return ToolJson.of(calls.read(
                "get_release", "{\"softwareId\": \"%s\", \"releaseId\": \"%s\"}".formatted(elite, ay), MEMBER));
    }

    private String screenshots(@Nullable UUID release, String... paths) {
        StringBuilder urls = new StringBuilder();
        for (String path : paths) {
            urls.append(urls.isEmpty() ? "" : ", ")
                    .append('"')
                    .append(site.url(path))
                    .append('"');
        }
        return "{\"softwareId\": \"%s\", \"releaseId\": %s, \"urls\": [%s]}"
                .formatted(elite, release == null ? "null" : "\"" + release + "\"", urls);
    }

    private String gallery(String gallery, @Nullable UUID release, String... paths) {
        String add = screenshots(release, paths);
        return add.substring(0, add.length() - 1) + ", \"gallery\": \"" + gallery + "\"}";
    }

    private String archive(@Nullable String fileName) {
        return "{\"softwareId\": \"%s\", \"releaseId\": \"%s\", \"url\": \"%s\", \"fileName\": %s}"
                .formatted(elite, ay, site.url("/download"), fileName == null ? "null" : "\"" + fileName + "\"");
    }
}
