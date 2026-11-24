package com.archive.backend.remote.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.remote.TestImages;
import com.archive.backend.remote.TestSite;
import com.archive.backend.remote.domain.RemoteRequestRefusedException;
import com.archive.backend.remote.read.DownloadedFile;
import com.archive.backend.remote.read.FetchedPage;
import com.archive.backend.remote.read.ViewedImage;
import java.net.Inet4Address;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Requests to other sites: pages read in their charset and cut at a limit, pictures judged by their bytes, files
 * downloaded under their names, and every address — the first and each redirect — passing the policy. Here the
 * policy lets only the IPv4 loopback through, where the test site runs.
 */
class GuardedRemoteResourcesTest {

    private static final Charset CP1251 = Charset.forName("windows-1251");

    private final TestSite site = TestSite.start();
    private final GuardedRemoteResources remote =
            new GuardedRemoteResources(address -> address instanceof Inet4Address && address.isLoopbackAddress());

    @AfterEach
    void stop() {
        site.close();
    }

    @Test
    void readsAPageInTheCharsetItsHeaderNames() {
        site.serve(
                "/page",
                200,
                Map.of("Content-Type", "text/html; charset=windows-1251"),
                "<p>Игра</p>".getBytes(CP1251));

        FetchedPage page = remote.fetchPage(site.url("/page"), 200_000);

        assertThat(page.status()).isEqualTo(200);
        assertThat(page.finalUrl()).isEqualTo(site.url("/page"));
        assertThat(page.contentType()).isEqualTo("text/html; charset=windows-1251");
        assertThat(page.body()).isEqualTo("<p>Игра</p>");
        assertThat(page.truncated()).isFalse();
    }

    @Test
    void readsThePageInTheCharsetOfItsMetaWhenTheHeaderNamesNone() {
        site.serve(
                "/page",
                200,
                Map.of("Content-Type", "text/html"),
                "<meta charset=\"windows-1251\"><p>Игра</p>".getBytes(CP1251));

        assertThat(remote.fetchPage(site.url("/page"), 200_000).body()).endsWith("<p>Игра</p>");
    }

    @Test
    void cutsALongPageAtTheLimitWithoutBreakingACharacter() {
        site.serve(
                "/long",
                200,
                Map.of("Content-Type", "text/plain; charset=utf-8"),
                "я".repeat(1000).getBytes(StandardCharsets.UTF_8));

        FetchedPage page = remote.fetchPage(site.url("/long"), 101);

        assertThat(page.truncated()).isTrue();
        assertThat(page.body()).isEqualTo("я".repeat(50));
    }

    @Test
    void readsAnErrorPageWithItsStatus() {
        site.serve("/missing", 404, Map.of("Content-Type", "text/plain"), "Not here".getBytes(StandardCharsets.UTF_8));

        FetchedPage page = remote.fetchPage(site.url("/missing"), 200_000);

        assertThat(page.status()).isEqualTo(404);
        assertThat(page.body()).isEqualTo("Not here");
    }

    @Test
    void givesOnlyTheTypeAndSizeOfContentThatIsNotText() {
        site.serve("/game.zip", 200, Map.of("Content-Type", "application/zip"), new byte[] {'P', 'K', 3, 4, 0});

        FetchedPage page = remote.fetchPage(site.url("/game.zip"), 200_000);

        assertThat(page.body()).isNull();
        assertThat(page.contentType()).isEqualTo("application/zip");
        assertThat(page.size()).isEqualTo(5);
    }

    @Test
    void followsRedirectsToTheLastPage() {
        site.redirect("/old", "/new")
                .serve("/new", 200, Map.of("Content-Type", "text/plain"), "Moved".getBytes(StandardCharsets.UTF_8));

        FetchedPage page = remote.fetchPage(site.url("/old"), 200_000);

        assertThat(page.finalUrl()).isEqualTo(site.url("/new"));
        assertThat(page.body()).isEqualTo("Moved");
    }

    @Test
    void refusesAnAddressThePolicyRefusesAlsoBehindARedirectAndByName() {
        site.redirect("/inside", "http://[::1]:1/secret");

        assertThatThrownBy(() -> remote.fetchPage(site.url("/inside"), 200_000))
                .isInstanceOf(RemoteRequestRefusedException.class)
                .hasMessageContaining("::1");
        assertThatThrownBy(() -> remote.fetchPage("http://[::1]:1/secret", 200_000))
                .isInstanceOf(RemoteRequestRefusedException.class);
        GuardedRemoteResources nowhere = new GuardedRemoteResources(address -> false);
        assertThatThrownBy(() -> nowhere.fetchPage(site.url("/inside").replace("127.0.0.1", "localhost"), 200_000))
                .isInstanceOf(RemoteRequestRefusedException.class)
                .hasMessageContaining("localhost");
    }

    @Test
    void refusesMoreThanFiveRedirects() {
        for (int i = 0; i < 6; i++) {
            site.redirect("/hop" + i, "/hop" + (i + 1));
        }
        site.serve("/hop6", 200, Map.of("Content-Type", "text/plain"), "End".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> remote.fetchPage(site.url("/hop0"), 200_000))
                .isInstanceOf(RemoteRequestRefusedException.class)
                .hasMessageContaining("redirects");
        assertThat(remote.fetchPage(site.url("/hop1"), 200_000).body()).isEqualTo("End");
    }

    @Test
    void viewsAPictureByItsBytes() {
        byte[] png = TestImages.png(4, 3);
        site.serve("/shot", 200, Map.of("Content-Type", "application/octet-stream"), png);

        ViewedImage image = remote.viewImage(site.url("/shot"), 5_000_000);

        assertThat(image.mediaType()).isEqualTo("image/png");
        assertThat(Base64.getDecoder().decode(image.data())).isEqualTo(png);
    }

    @Test
    void refusesToViewWhatIsNoPictureIsTooLargeOrIsNotThere() {
        site.serve("/text", 200, Map.of("Content-Type", "image/png"), "<html>".getBytes(StandardCharsets.UTF_8))
                .serve("/big", 200, Map.of("Content-Type", "image/png"), TestImages.png(200, 200))
                .serve("/gone", 404, Map.of(), TestImages.png(4, 3));

        assertThatThrownBy(() -> remote.viewImage(site.url("/text"), 5_000_000))
                .isInstanceOf(RemoteRequestRefusedException.class);
        assertThatThrownBy(() -> remote.viewImage(site.url("/big"), 100))
                .isInstanceOf(RemoteRequestRefusedException.class);
        assertThatThrownBy(() -> remote.viewImage(site.url("/gone"), 5_000_000))
                .isInstanceOf(RemoteRequestRefusedException.class)
                .hasMessageContaining("404");
    }

    @Test
    void downloadsAFileUnderTheNameTheSiteGivesItAndDeletesItOnceClosed() throws Exception {
        byte[] zip = {'P', 'K', 3, 4, 1, 2, 3};
        site.serve(
                        "/get",
                        200,
                        Map.of("Content-Disposition", "attachment; filename*=UTF-8''%D0%B8%D0%B3%D1%80%D0%B0.zip"),
                        zip)
                .serve("/files/Elite AY.zip", 200, Map.of(), zip);

        Path path;
        try (DownloadedFile file = remote.download(site.url("/get"), 1_000)) {
            path = file.path();
            assertThat(file.fileName()).isEqualTo("игра.zip");
            assertThat(file.size()).isEqualTo(7);
            assertThat(Files.readAllBytes(path)).isEqualTo(zip);
        }
        assertThat(path).doesNotExist();
        try (DownloadedFile file = remote.download(site.url("/files/Elite%20AY.zip"), 1_000)) {
            assertThat(file.fileName()).isEqualTo("Elite AY.zip");
        }
    }

    @Test
    void refusesADownloadOverItsLimitOrNotThereLeavingNoFile() throws Exception {
        site.serve("/big.zip", 200, Map.of(), new byte[2_000]).serve("/gone.zip", 404, Map.of(), new byte[10]);
        long before = temporaryFiles();

        assertThatThrownBy(() -> remote.download(site.url("/big.zip"), 1_000))
                .isInstanceOf(RemoteRequestRefusedException.class)
                .hasMessageContaining("1000");
        assertThatThrownBy(() -> remote.download(site.url("/gone.zip"), 1_000))
                .isInstanceOf(RemoteRequestRefusedException.class)
                .hasMessageContaining("404");
        assertThat(temporaryFiles()).isEqualTo(before);
    }

    private static long temporaryFiles() throws Exception {
        Path folder = GuardedRemoteResources.TEMPORARY_FOLDER;
        if (!Files.exists(folder)) {
            return 0;
        }
        try (var files = Files.list(folder)) {
            return files.count();
        }
    }
}
