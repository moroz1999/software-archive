package com.archive.backend.importer.infra;

import static com.archive.backend.importer.infra.FakeSourceHttp.list;
import static com.archive.backend.importer.infra.FakeSourceHttp.page;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.importer.domain.vektor.VektorListPage;
import com.archive.backend.importer.domain.vektor.VektorListing;
import com.archive.backend.importer.domain.vektor.VektorSection;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class HttpVektorSiteTest {

    private static final String CATALOG = "https://vektor-06c.ru/programmnoe-obespechenie/";

    private final FakeSourceHttp http = new FakeSourceHttp();
    private final HttpVektorSite site = new HttpVektorSite(http);

    @Test
    void readsTheFirstPageOfTheWholeCatalogWithTheAddressesOfTheOthers() {
        http.serve(CATALOG + "?PAGEN_2=1", page("list-1.html"));

        VektorListPage first = site.firstPage(null);

        assertThat(first.url()).isEqualTo(CATALOG + "?PAGEN_2=1");
        assertThat(first.listings())
                .hasSize(10)
                .first()
                .isEqualTo(new VektorListing("408", CATALOG + "kartochnye-igry_/101-ochko/"));
        assertThat(first.otherPages()).hasSize(84).startsWith(CATALOG + "?PAGEN_2=2");
    }

    @Test
    void readsTheFirstPageOfASectionWithTheAddressesItsPaginationGives() {
        http.serve(CATALOG + "arkadnye-igry/", page("section-arkadnye-igry.html"));

        VektorListPage first = site.firstPage(new VektorSection("arkadnye-igry"));

        assertThat(first.listings())
                .extracting(VektorListing::id)
                .startsWith("49")
                .hasSize(10);
        assertThat(first.otherPages())
                .hasSize(23)
                .startsWith(CATALOG + "arkadnye-igry/?PAGEN_1=2")
                .endsWith(CATALOG + "arkadnye-igry/?PAGEN_1=24");
    }

    @Test
    void readsASectionOfOnePage() {
        http.serve(CATALOG + "zagruzchiki/", page("section-zagruzchiki.html"));

        VektorListPage first = site.firstPage(new VektorSection("zagruzchiki"));

        assertThat(first.listings()).extracting(VektorListing::id).containsExactly("599", "598", "595");
        assertThat(first.otherPages()).isEmpty();
        assertThat(http.asked()).containsExactly(CATALOG + "zagruzchiki/");
    }

    @Test
    void readsAnotherPageOfAListByItsAddress() {
        String second = CATALOG + "arkadnye-igry/?PAGEN_1=2";
        http.serve(second, list("736", "/programmnoe-obespechenie/arkadnye-igry/athletic-land/"));

        VektorListPage page = site.page(second);

        assertThat(page.url()).isEqualTo(second);
        assertThat(page.listings()).extracting(VektorListing::id).containsExactly("736");
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
