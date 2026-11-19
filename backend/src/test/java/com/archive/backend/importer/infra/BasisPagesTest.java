package com.archive.backend.importer.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.importer.domain.basis.BasisAuthor;
import com.archive.backend.importer.domain.basis.BasisListing;
import com.archive.backend.importer.domain.basis.BasisProgram;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pages as the card index "Базис" at caglrc.cc served them. */
class BasisPagesTest {

    private static final String WARE = "https://caglrc.cc/scalar/ware/";
    private static final String MEDIA = "https://caglrc.cc/scalar/media/";

    @Test
    void readsEveryCardOfTheWholeListInItsOrder() {
        List<BasisListing> listings = BasisPages.listings(FakeSourceHttp.basisPage("list-all.html"));

        assertThat(listings).hasSize(942);
        assertThat(listings.subList(0, 2))
                .containsExactly(new BasisListing("1", WARE + "1/"), new BasisListing("2", WARE + "2/"));
    }

    @Test
    void readsTheCardOfAProgram() {
        BasisProgram program =
                BasisPages.program(FakeSourceHttp.basisPage("ware-418.html"), new BasisListing("418", WARE + "418/"));

        assertThat(program)
                .isEqualTo(new BasisProgram(
                        "418",
                        WARE + "418/",
                        "Piton",
                        "1991",
                        List.of("arcade", "game"),
                        "Ваша цель — управляя стрелками съесть все синие точки, обходя красные. При этом нельзя"
                                + " натыкаться на границы экрана и хвост.",
                        List.of(MEDIA + "s/pitongln1.png", MEDIA + "s/pitongln2.png"),
                        List.of("21"),
                        MEDIA + "w/pitongln.rom"));
    }

    @Test
    void readsACardWithoutAYearAFileOrScreenshotsAndTheTextOfItsLinks() {
        BasisProgram program =
                BasisPages.program(FakeSourceHttp.basisPage("ware-231.html"), new BasisListing("231", WARE + "231/"));

        assertThat(program.year()).isEmpty();
        assertThat(program.categories()).containsExactly("demo", "missing");
        assertThat(program.description()).isEqualTo("О существовании этого слайдшоу упоминается в прон-шоу To Rise.");
        assertThat(program.screenshotUrls()).isEmpty();
        assertThat(program.fileUrl()).isNull();
    }

    @Test
    void readsEveryAuthorOfACardInTheirOrder() {
        BasisProgram program =
                BasisPages.program(FakeSourceHttp.basisPage("ware-558.html"), new BasisListing("558", WARE + "558/"));

        assertThat(program.authorIds()).containsExactly("215", "3");
    }

    @Test
    void readsTheNameTheNicknameAndThePlaceOfAnAuthor() {
        assertThat(BasisPages.author(FakeSourceHttp.basisPage("author-5.html"), "5"))
                .isEqualTo(new BasisAuthor("5", "Ермолаев Сергей", "S.E.S.", "Санкт-Петербург"));
        assertThat(BasisPages.author(FakeSourceHttp.basisPage("author-33.html"), "33"))
                .isEqualTo(new BasisAuthor("33", "Луппов Г.Б.", null, "Киров"));
    }
}
