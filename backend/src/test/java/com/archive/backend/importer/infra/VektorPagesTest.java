package com.archive.backend.importer.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.importer.domain.vektor.VektorListing;
import com.archive.backend.importer.domain.vektor.VektorProgram;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pages as vektor-06c.ru served them, without the debug dump some of them carry.
 */
class VektorPagesTest {

    private static final String LIST = "https://vektor-06c.ru/programmnoe-obespechenie/";

    @Test
    void readsTheProgramsOfAListInTheirOrder() {
        List<VektorListing> listings = VektorPages.listings(page("list-1.html"));

        assertThat(listings).hasSize(10);
        assertThat(listings.subList(0, 3))
                .containsExactly(
                        new VektorListing("408", LIST + "kartochnye-igry_/101-ochko/"),
                        new VektorListing("708", LIST + "demo-programmy/256-tsvetov/"),
                        new VektorListing("524", LIST + "demo-programmy/512-demo/"));
    }

    @Test
    void readsTheProgramsOfASection() {
        assertThat(VektorPages.listings(page("section-zagruzchiki.html")))
                .extracting(VektorListing::id)
                .containsExactly("599", "598", "595");
    }

    @Test
    void readsTheNumberOfTheLastPage() {
        assertThat(VektorPages.lastPage(page("list-1.html"))).isEqualTo(85);
        assertThat(VektorPages.lastPage(page("list-85.html"))).isEqualTo(85);
        assertThat(VektorPages.lastPage(page("section-zagruzchiki.html"))).isEqualTo(1);
    }

    @Test
    void readsTheCardOfAProgram() {
        VektorProgram program = VektorPages.program(
                page("program-524.html"), new VektorListing("524", LIST + "demo-programmy/512-demo/"));

        assertThat(program)
                .isEqualTo(new VektorProgram(
                        "524",
                        LIST + "demo-programmy/512-demo/",
                        "demo-programmy",
                        "512 demo",
                        "Азарёнок П./P.G.A.",
                        "2009",
                        "Небольшое демо (512 байт) с исходниками.",
                        "https://vektor-06c.ru/upload/iblock/cf4/rl2zy0k83l0ml8asvhd7jkg5tzkmvip8/512demo1_.gif",
                        "https://vektor-06c.ru/upload/iblock/0a6/j65p6436yygw71tcz2pvsxxlc7jl58y3/512demo.zip",
                        "512demo.zip",
                        List.of("512demo.r0m", "512demo.asm")));
    }

    @Test
    void readsEveryFileOfTheArchive() {
        VektorProgram program = VektorPages.program(
                page("program-243.html"), new VektorListing("243", LIST + "arkadnye-igry/dengolov/"));

        assertThat(program.archiveFiles()).containsExactly("dengol17.rom", "dengolos.rom", "dengolos_(r).rom");
        assertThat(program.archiveName()).isEqualTo("dengolov_.zip");
    }

    @Test
    void readsAnArchiveWhoseFilesTheSiteDoesNotList() {
        VektorProgram program = VektorPages.program(
                page("program-763.html"), new VektorListing("763", LIST + "operatsionnye-sistemy/boldbios/"));

        assertThat(program.archiveFiles()).isEmpty();
        assertThat(program.archiveUrl())
                .isEqualTo("https://vektor-06c.ru/upload/iblock/d51/u9dnlkeaa4h8k97icutxd334lz320mz6/BoldBIOS.zip");
    }

    @Test
    void trimsTheTitleAndKeepsTheLinesOfTheDescription() {
        VektorProgram program = VektorPages.program(
                page("program-754.html"),
                new VektorListing("754", LIST + "operatsionnye-sistemy/mikrodos-3-1-bios-3-11-f-11/"));

        assertThat(program.title()).isEqualTo("МикроДОС 3.1 (BIOS 3.11.F.11)");
        assertThat(program.authors()).isEqualTo("Неизвестный Автор,  Филиппов Е.В.");
        assertThat(program.year()).isEqualTo("-");
        assertThat(program.description()).isEqualTo("A,B - FDD\nC - EDD");
    }

    @Test
    void takesTheSectionFromTheAddressNotFromTheCard() {
        VektorProgram program = VektorPages.program(
                page("program-892.html"), new VektorListing("892", LIST + "arkadnye-igry/joe-blade-/"));

        assertThat(program.section()).isEqualTo("arkadnye-igry");
        assertThat(program.title()).isEqualTo("Joe Blade");
        assertThat(program.year()).isEqualTo("1987 / 2026");
        assertThat(program.description())
                .startsWith("Игроку предстоит управлять персонажем, проходя сквозь комнаты, уничтожая врагов, спасая"
                        + " мирных людей, обезвреживая бомбы\nДжо Блейд");
    }

    @Test
    void keepsTheAuthorsLineAsWrittenForTheirSplitting() {
        VektorProgram program = VektorPages.program(
                page("program-753.html"), new VektorListing("753", LIST + "operatsionnye-sistemy/cpm-59/"));

        assertThat(program.authors()).isEqualTo("Графов А. ,  Синицын А.");
        assertThat(program.archiveFiles()).containsExactly("cpm256.rom", "cpm512.rom", "cpm768.rom", "cpm59.doc");
    }

    @Test
    void leavesOutThePictureThatSaysThereIsNoScreenshot() {
        VektorProgram program = VektorPages.program(
                page("program-763.html"), new VektorListing("763", LIST + "operatsionnye-sistemy/boldbios/"));

        assertThat(program.screenshotUrl()).isNull();
    }

    private static String page(String name) {
        try (InputStream in = VektorPagesTest.class.getResourceAsStream("/importer/vektor/" + name)) {
            if (in == null) {
                throw new IllegalStateException("No test page " + name);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
