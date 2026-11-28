package com.archive.backend.assistant.web.remote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.IntegrationTest;
import com.archive.backend.assistant.web.tool.ToolArgumentsException;
import com.archive.backend.assistant.web.tool.ToolCalls;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolJson;
import com.archive.backend.remote.TestImages;
import com.archive.backend.remote.TestSite;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.user.TestActors;
import com.jayway.jsonpath.DocumentContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/** Reading pages and looking at pictures of other sites, and at the screenshots of the archive. */
@IntegrationTest
@Transactional
@Rollback
class RemoteToolsIT {

    private static final ToolContext MEMBER = new ToolContext(TestActors.MEMBER, Language.EN);

    @Autowired
    private ToolCalls calls;

    @Autowired
    private JdbcTemplate jdbc;

    private final TestSite site = TestSite.start();
    private final byte[] png = TestImages.png(8, 6);

    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        site.serve(
                        "/game",
                        200,
                        Map.of("Content-Type", "text/html; charset=utf-8"),
                        "<h1>Elite</h1>".getBytes(StandardCharsets.UTF_8))
                .serve("/shot.png", 200, Map.of("Content-Type", "image/png"), png)
                .serve("/shot.bmp", 200, Map.of("Content-Type", "image/bmp"), TestImages.bmp(8, 6));
    }

    @AfterEach
    void stop() {
        site.close();
    }

    @Test
    void readsAPageOfAnotherSite() {
        DocumentContext page = ToolJson.of(calls.read("fetch_url", url("/game"), MEMBER));

        assertThat(page.read("$.status", Integer.class)).isEqualTo(200);
        assertThat(page.read("$.finalUrl", String.class)).isEqualTo(site.url("/game"));
        assertThat(page.read("$.body", String.class)).isEqualTo("<h1>Elite</h1>");
    }

    @Test
    void refusesAnAddressThatIsNoWebAddress() {
        assertThatThrownBy(() -> calls.read("fetch_url", "{\"url\": \"file:///etc/passwd\"}", MEMBER))
                .isInstanceOf(ToolArgumentsException.class)
                .hasMessageContaining("url");
    }

    @Test
    void looksAtAPictureOfAnotherSite() {
        DocumentContext image = ToolJson.of(calls.read("view_image", url("/shot.png"), MEMBER));

        assertThat(image.read("$.mediaType", String.class)).isEqualTo("image/png");
        assertThat(Base64.getDecoder().decode(image.read("$.data", String.class)))
                .isEqualTo(png);
    }

    @Test
    void copiesABmpScreenshotAndShowsItToTheModelAsAPng() throws Exception {
        UUID software = calls.write("create_software", "{\"title\": \"Elite\", \"platform\": \"vector\"}", MEMBER)
                .uuid();
        calls.write("add_screenshots", """
                {"softwareId": "%s", "releaseId": null, "urls": ["%s"]}
                """.formatted(software, site.url("/shot.bmp")), MEMBER);
        String screenshot = ToolJson.of(calls.read("get_software", "{\"id\": \"%s\"}".formatted(software), MEMBER))
                .read("$.screenshots[0].id", String.class);

        for (String arguments : new String[] {url("/shot.bmp"), "{\"screenshotId\": \"%s\"}".formatted(screenshot)}) {
            DocumentContext image = ToolJson.of(calls.read("view_image", arguments, MEMBER));

            assertThat(image.read("$.mediaType", String.class)).isEqualTo("image/png");
            BufferedImage shown = ImageIO.read(
                    new ByteArrayInputStream(Base64.getDecoder().decode(image.read("$.data", String.class))));
            assertThat(shown.getWidth()).isEqualTo(8);
            assertThat(shown.getHeight()).isEqualTo(6);
        }
    }

    @Test
    void looksAtAScreenshotOfTheArchive() {
        UUID software = calls.write("create_software", "{\"title\": \"Elite\", \"platform\": \"vector\"}", MEMBER)
                .uuid();
        calls.write("add_screenshots", """
                {"softwareId": "%s", "releaseId": null, "urls": ["%s"]}
                """.formatted(software, site.url("/shot.png")), MEMBER);
        String screenshot = ToolJson.of(calls.read("get_software", "{\"id\": \"%s\"}".formatted(software), MEMBER))
                .read("$.screenshots[0].id", String.class);

        DocumentContext image =
                ToolJson.of(calls.read("view_image", "{\"screenshotId\": \"%s\"}".formatted(screenshot), MEMBER));

        assertThat(image.read("$.mediaType", String.class)).isEqualTo("image/png");
        assertThat(Base64.getDecoder().decode(image.read("$.data", String.class)))
                .isEqualTo(png);
    }

    @Test
    void looksAtOnePictureAtATime() {
        assertThatThrownBy(() -> calls.read("view_image", "{}", MEMBER)).isInstanceOf(ToolArgumentsException.class);
        assertThatThrownBy(() -> calls.read(
                        "view_image",
                        "{\"url\": \"%s\", \"screenshotId\": \"%s\"}"
                                .formatted(site.url("/shot.png"), UUID.randomUUID()),
                        MEMBER))
                .isInstanceOf(ToolArgumentsException.class);
        assertThatThrownBy(() ->
                        calls.read("view_image", "{\"screenshotId\": \"%s\"}".formatted(UUID.randomUUID()), MEMBER))
                .isInstanceOf(NotFoundException.class);
    }

    private String url(String path) {
        return "{\"url\": \"%s\"}".formatted(site.url(path));
    }
}
