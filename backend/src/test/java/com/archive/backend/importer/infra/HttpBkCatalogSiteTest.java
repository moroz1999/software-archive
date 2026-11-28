package com.archive.backend.importer.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.importer.domain.bkcatalog.BkCatalogEntry;
import com.archive.backend.importer.domain.bkcatalog.BkCatalogSection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** bk-catalog read from its repository on GitHub. */
class HttpBkCatalogSiteTest {

    private static final String FILES = "https://raw.githubusercontent.com/kalininskiy/bk-catalog/main/";

    private final FakeSourceHttp http = new FakeSourceHttp();
    private final HttpBkCatalogSite site = new HttpBkCatalogSite(http);

    @AfterEach
    void clear() {
        http.clear();
    }

    @Test
    void readsTheListOfASectionFromItsCsv() {
        http.serve(FILES + "content/demoscene.csv", "ID,Название\n4000108,3D WIREFRAME SHIPS FROM ELITE\n");

        assertThat(site.entries(BkCatalogSection.DEMOSCENE))
                .containsExactly(new BkCatalogEntry(
                        BkCatalogSection.DEMOSCENE,
                        Map.of("ID", "4000108", "Название", "3D WIREFRAME SHIPS FROM ELITE")));
    }

    @Test
    void downloadsAFileOfTheRepositoryByItsPath() {
        http.serve(FILES + "bk_games_files/digger.zip", "zip".getBytes(StandardCharsets.UTF_8));

        assertThat(site.download("bk_games_files/digger.zip")).hasValue("zip".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void asksForANameAsItIsWrittenEvenWithMarksAnAddressWouldReadOtherwise() {
        http.serve(FILES + "bk_files/4_colors_are_enough%2521.zip", new byte[] {1});
        http.serve(FILES + "bk_files/CSIDOS.ver.3.20.Multi%20(rus).zip", new byte[] {2});
        http.serve(FILES + "bk_files/%D0%91%D0%9A.zip", new byte[] {3});

        assertThat(site.download("bk_files/4_colors_are_enough%21.zip")).hasValue(new byte[] {1});
        assertThat(site.download("bk_files/CSIDOS.ver.3.20.Multi (rus).zip")).hasValue(new byte[] {2});
        assertThat(site.download("bk_files/БК.zip")).hasValue(new byte[] {3});
    }

    @Test
    void hasNoFileTheRepositoryLacks() {
        assertThat(site.download("bk_games_files/lost.zip")).isEmpty();
    }

    @Test
    void failsWhenTheRepositoryCannotBeRead() {
        http.fail(FILES + "bk_games_files/digger.zip");

        assertThatThrownBy(() -> site.download("bk_games_files/digger.zip")).isInstanceOf(IllegalStateException.class);
    }
}
