package com.archive.backend.importer.domain.basis;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.author.domain.RealName;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.source.SourceAuthor;
import com.archive.backend.importer.domain.source.SourceAuthorKind;
import com.archive.backend.importer.domain.source.SourcePlace;
import com.archive.backend.importer.domain.source.SourcePlaceKind;
import com.archive.backend.importer.domain.source.SourceProgram;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.software.domain.PreservationStatus;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ReleaseDate;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

/** A program of "Базис" read by the rules of the site (docs/domain/imports.md#caglrccc-scalar). */
class BasisSourceProgramsTest {

    private static final String WARE = "https://caglrc.cc/scalar/ware/";
    private static final String MEDIA = "https://caglrc.cc/scalar/media/";
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0};

    private static final CategoryId GAMES = category("081bf3a1-782b-5a8b-8d12-3b155e0a58cb");
    private static final CategoryId ARCADE = category("8536e450-1fc7-5985-9cd2-97b499713a67");
    private static final CategoryId DEMO = category("936b5821-6c89-54c5-93f0-a21ffb1580b3");
    private static final CategoryId WORD_PROCESSOR = category("e106c954-4886-50c1-b261-4185033b4a1c");

    private final Site site = new Site(Map.of(
            "5", new BasisAuthor("5", "Ермолаев Сергей", "S.E.S.", "Санкт-Петербург"),
            "33", new BasisAuthor("33", "Луппов Г.Б.", null, "Киров"),
            "10", new BasisAuthor("10", "Triangle Union", "Triangle Union", "Днестровск"),
            "20", new BasisAuthor("20", "Неизвестный Автор", null, "Нигде"),
            "77", new BasisAuthor("77", "Безвестный", null, "Нигде")));

    @Test
    void offersEveryCategoryThatCanContainImportedSoftware() {
        assertThat(BasisCategory.importable())
                .contains(new BasisCategory("arcade"), new BasisCategory("3D"))
                .doesNotContain(new BasisCategory("pc"), new BasisCategory("document"));
    }

    @Test
    void takesTheTitleTheYearTheDescriptionAndThePageOfTheProgram() {
        SourceProgram program = BasisSourcePrograms.of(program("418", "1991", List.of("game")), site);

        assertThat(program.archive()).isEqualTo("caglrc.cc");
        assertThat(program.id()).isEqualTo("418");
        assertThat(program.page()).isEqualTo(new ExternalPage(WARE + "418/"));
        assertThat(program.title().value()).isEqualTo("Piton");
        assertThat(program.description()).isNotNull();
        assertThat(program.softwareDate()).isEqualTo(ReleaseDate.ofYear(Year.of(1991)));
        assertThat(program.releaseDate()).isNull();
        assertThat(program.platform().value()).isEqualTo("vector");
    }

    @Test
    void leavesTheDateUnknownWithoutAYear() {
        assertThat(BasisSourcePrograms.of(program("231", "", List.of("demo")), site)
                        .softwareDate())
                .isNull();
    }

    @Test
    void placesTheProgramInTheCategoriesOfTheArchiveAndIgnoresTheMarksThatAreNoGenre() {
        SourceProgram program = BasisSourcePrograms.of(
                program("1", "", List.of("game", "arcade", "demo", "text", "msx", "zx", "3D", "something-new")), site);

        assertThat(program.categories()).containsExactlyInAnyOrder(GAMES, ARCADE, DEMO, WORD_PROCESSOR);
    }

    @Test
    void isForTheVector06cUnlessACategoryNamesAnotherComputerOfTheFamily() {
        assertThat(BasisSourcePrograms.of(program("1", "", List.of("game")), site)
                        .hardware())
                .containsExactly(code("vector06c"));
        assertThat(BasisSourcePrograms.of(program("1", "", List.of("game", "krista-2")), site)
                        .hardware())
                .containsExactly(code("krista2"));
        assertThat(BasisSourcePrograms.of(program("1", "", List.of("start-1200")), site)
                        .hardware())
                .containsExactly(code("start1200"));
        assertThat(BasisSourcePrograms.of(program("1", "", List.of("pk-6128c")), site)
                        .hardware())
                .containsExactly(code("pk6128c"));
    }

    @Test
    void takesTheHardwareTheProgramUsesForTheSoftwareAndTheStorageForItsRelease() {
        SourceProgram program = BasisSourcePrograms.of(
                program(
                        "1",
                        "",
                        List.of(
                                "tape",
                                "disk",
                                "ay",
                                "beeper",
                                "vi53-music",
                                "joystick_p",
                                "joystick_s",
                                "hdd",
                                "kvaz",
                                "barkar",
                                "z80")),
                site);

        assertThat(program.hardware())
                .containsExactlyInAnyOrder(
                        code("vector06c"),
                        code("ay8910"),
                        code("beeper"),
                        code("vi53"),
                        code("joystick-p"),
                        code("joystick-s"),
                        code("hdd"),
                        code("kvaz"),
                        code("barkar"),
                        code("z80"));
        assertThat(program.releaseHardware()).containsExactlyInAnyOrder(code("tape"), code("floppy"));
    }

    @Test
    void tellsAMissingProgramIsMissingInAction() {
        assertThat(BasisSourcePrograms.of(program("231", "", List.of("demo", "missing")), site)
                        .preservation())
                .isEqualTo(PreservationStatus.MISSING_IN_ACTION);
        assertThat(BasisSourcePrograms.of(program("1", "", List.of("demo")), site)
                        .preservation())
                .isEqualTo(PreservationStatus.UNKNOWN);
    }

    @Test
    void leavesOutSoftwareForThePcAndPaper() {
        assertThat(BasisSourcePrograms.imported(program("1", "", List.of("utility", "pc"))))
                .isFalse();
        assertThat(BasisSourcePrograms.imported(program("1", "", List.of("magazine"))))
                .isFalse();
        assertThat(BasisSourcePrograms.imported(program("1", "", List.of("document", "pk-6128c"))))
                .isFalse();
        assertThat(BasisSourcePrograms.imported(program("1", "", List.of("catalogue"))))
                .isFalse();
        assertThat(BasisSourcePrograms.imported(program("1", "", List.of("game", "missing"))))
                .isTrue();
    }

    @Test
    void creditsAPersonAGroupOrALabelOfNobodyAsTheAuthorPageTellsAndNobodyForTheUnknownAuthor() {
        SourceProgram program =
                BasisSourcePrograms.of(program("1", "", List.of("demo"), List.of("5", "10", "33", "20", "77")), site);

        assertThat(program.authors())
                .containsExactly(
                        new SourceAuthor(
                                "5",
                                new LabelName("S.E.S."),
                                SourceAuthorKind.PERSON,
                                new RealName("Ермолаев Сергей"),
                                place("Санкт-Петербург")),
                        new SourceAuthor(
                                "10",
                                new LabelName("Triangle Union"),
                                SourceAuthorKind.GROUP,
                                null,
                                place("Днестровск")),
                        new SourceAuthor(
                                "33", new LabelName("Луппов Г.Б."), SourceAuthorKind.LABEL, null, place("Киров")),
                        new SourceAuthor("77", new LabelName("Безвестный"), SourceAuthorKind.LABEL, null, null));
    }

    @Test
    void downloadsEveryScreenshotAndTheFileUnderTheNamesTheSiteGivesThem() {
        BasisProgram basis = new BasisProgram(
                "418",
                WARE + "418/",
                "Piton",
                "1991",
                List.of("game"),
                null,
                List.of(MEDIA + "s/pitongln1.png", MEDIA + "s/pitongln2.png"),
                List.of(),
                MEDIA + "w/pitongln.rom");

        SourceProgram program = BasisSourcePrograms.of(basis, site);

        assertThat(program.screenshots())
                .extracting(image -> image.name().value())
                .containsExactly("pitongln1.png", "pitongln2.png");
        ReleaseArchiveUpload file = Objects.requireNonNull(program.file());
        assertThat(file.fileName()).isEqualTo("pitongln.rom");
        assertThat(site.downloaded)
                .containsExactly(MEDIA + "s/pitongln1.png", MEDIA + "s/pitongln2.png", MEDIA + "w/pitongln.rom");
    }

    @Test
    void hasNoFileWhenTheCardHasNone() {
        assertThat(BasisSourcePrograms.of(program("231", "", List.of("demo")), site)
                        .file())
                .isNull();
    }

    private static BasisProgram program(String id, String year, List<String> categories) {
        return program(id, year, categories, List.of());
    }

    private static BasisProgram program(String id, String year, List<String> categories, List<String> authors) {
        return new BasisProgram(
                id, WARE + id + "/", "Piton", year, categories, "Eat the dots.", List.of(), authors, null);
    }

    private static SourcePlace place(String name) {
        return new SourcePlace(name, Language.RU, SourcePlaceKind.CITY_OR_COUNTRY);
    }

    private static HardwareCode code(String value) {
        return new HardwareCode(value);
    }

    private static CategoryId category(String id) {
        return new CategoryId(UUID.fromString(id));
    }

    /** "Базис" with these author pages; any file it keeps is a picture or a program by its name. */
    private static final class Site implements BasisSite {

        private final Map<String, BasisAuthor> authors;
        private final List<String> downloaded = new ArrayList<>();

        private Site(Map<String, BasisAuthor> authors) {
            this.authors = authors;
        }

        @Override
        public List<BasisListing> listings(@Nullable BasisCategory category) {
            return List.of();
        }

        @Override
        public BasisProgram program(BasisListing listing) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BasisAuthor author(String id) {
            BasisAuthor author = authors.get(id);
            if (author == null) {
                throw new IllegalStateException("No author " + id);
            }
            return author;
        }

        @Override
        public byte[] download(String url) {
            downloaded.add(url);
            return url.endsWith(".png") ? PNG : "program".getBytes(StandardCharsets.UTF_8);
        }
    }
}
