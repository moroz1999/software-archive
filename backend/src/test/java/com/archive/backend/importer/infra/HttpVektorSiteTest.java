package com.archive.backend.importer.infra;

import static com.archive.backend.importer.infra.FakeVektorHttp.list;
import static com.archive.backend.importer.infra.FakeVektorHttp.page;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.importer.domain.vektor.VektorListing;
import com.archive.backend.importer.domain.vektor.VektorSection;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class HttpVektorSiteTest {

    private static final String CATALOG = "https://vektor-06c.ru/programmnoe-obespechenie/";

    private final FakeVektorHttp http = new FakeVektorHttp();
    private final HttpVektorSite site = new HttpVektorSite(http);

    @Test
    void walksEveryPageOfTheWholeCatalog() {
        http.serve(CATALOG + "?PAGEN_2=1", page("list-1.html"));
        for (int number = 2; number <= 85; number++) {
            http.serve(
                    CATALOG + "?PAGEN_2=" + number, list("1" + number, "/programmnoe-obespechenie/x/p" + number + "/"));
        }

        assertThat(site.listings(null))
                .hasSize(10 + 84)
                .first()
                .isEqualTo(new VektorListing("408", CATALOG + "kartochnye-igry_/101-ochko/"));
        assertThat(http.asked()).hasSize(85);
    }

    @Test
    void walksTheListOfOneSection() {
        http.serve(CATALOG + "zagruzchiki/", page("section-zagruzchiki.html"));

        assertThat(site.listings(new VektorSection("zagruzchiki")))
                .extracting(VektorListing::id)
                .containsExactly("599", "598", "595");
        assertThat(http.asked()).containsExactly(CATALOG + "zagruzchiki/");
    }

    @Test
    void readsThePageOfAProgram() {
        VektorListing listing = new VektorListing("524", CATALOG + "demo-programmy/512-demo/");
        http.serve(listing.url(), page("program-524.html"));

        assertThat(site.program(listing).title()).isEqualTo("512 demo");
    }

    @Test
    void downloadsAFileAsItIs() {
        http.serve("https://vektor-06c.ru/upload/a.zip", "PK".getBytes(StandardCharsets.US_ASCII));

        assertThat(site.download("https://vektor-06c.ru/upload/a.zip"))
                .isEqualTo("PK".getBytes(StandardCharsets.US_ASCII));
    }
}
