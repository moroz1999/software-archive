package com.archive.backend.importer.domain.bkcatalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.source.SourceAuthor;
import com.archive.backend.importer.domain.source.SourceProgram;
import com.archive.backend.importer.domain.source.SourceRelease;
import com.archive.backend.importer.domain.source.SourceWarning;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.software.domain.LineageKind;
import com.archive.backend.software.domain.LineageReliability;
import com.archive.backend.software.domain.LineageRework;
import com.archive.backend.software.domain.LineageSource;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.ReleaseDescription;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareLineage;
import com.archive.backend.software.domain.SoftwareLink;
import com.archive.backend.software.domain.SoftwareLinkType;
import com.archive.backend.software.domain.SoftwareUrl;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** An entry of bk-catalog read by the rules of the site (docs/domain/imports.md#bk-catalog). */
class BkCatalogSourceProgramsTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0};
    private static final byte[] BIGGER_PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0, 0};
    private static final byte[] ZIP = {'P', 'K', 3, 4};

    private final Site site = new Site();

    /** The file the tests of dates give their games, so that it is no missing file. */
    @BeforeEach
    void serveAFile() {
        site.files.put("bk_games_files/a.zip", ZIP);
    }

    @ParameterizedTest
    @CsvSource({
        "GAMES, game-555901, https://kalininskiy.github.io/bk-catalog/#game-555901",
        "SOFTWARE, software-555901, https://kalininskiy.github.io/bk-catalog/#software-555901",
        "DEMOSCENE, demo-555901, https://kalininskiy.github.io/bk-catalog/#demo-555901"
    })
    void isKnownByItsSectionAndIdAndLinksBackToItsCard(BkCatalogSection section, String id, String card) {
        SourceProgram program = BkCatalogSourcePrograms.of(entry(section, Map.of("ID", "555901")), site);

        assertThat(program.archive()).isEqualTo("bk-catalog");
        assertThat(program.id()).isEqualTo(id);
        assertThat(program.page()).isEqualTo(new ExternalPage(card));
    }

    @Test
    void takesTheTitleAndTheDescriptionOfTheBk() {
        SourceProgram program = BkCatalogSourcePrograms.of(
                game(Map.of("Название", "Artillery 2026", "Описание", "  Игра на Бейсике.  ")), site);

        assertThat(program.title().value()).isEqualTo("Artillery 2026");
        assertThat(program.description()).isEqualTo(new SoftwareDescription("Игра на Бейсике."));
        assertThat(program.platform().value()).isEqualTo("bk");
    }

    @Test
    void hasNoDescriptionWhenTheEntryGivesNone() {
        assertThat(BkCatalogSourcePrograms.of(game(Map.of()), site).description())
                .isNull();
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                "БК0010 | bk0010",
                "БК0010-01 | bk0010-01",
                "БК0010-01 БЕЙСИК | bk0010-01 basic",
                "БК0010 ФОКАЛ | bk0010 focal",
                "БК0010 БЕЙСИК 2026 | bk0010 basic",
                "БК0011 БЕЙСИК | bk0011 basic",
                "БК0011 ФОКАЛ | bk0011 focal",
                "БК0011М | bk0011m",
                "БК0011М + СМК512 | bk0011m smk512",
                "БК0010 + СМК512 | bk0010 smk512",
                "БК0010 (11М) | bk0010 bk0011m",
                "AZБК | azbk"
            })
    void takesTheComputersAndTheFirmwareThePlatformNames(String platform, String hardware) {
        SourceProgram program = BkCatalogSourcePrograms.of(game(Map.of("Платформа", platform)), site);

        assertThat(program.hardware())
                .containsExactlyInAnyOrderElementsOf(Arrays.stream(hardware.split(" "))
                        .map(HardwareCode::new)
                        .toList());
        assertThat(program.warnings()).isEmpty();
    }

    @Test
    void namesNoComputerOfAnEntryWithoutAPlatform() {
        SourceProgram program = BkCatalogSourcePrograms.of(entry(BkCatalogSection.SOFTWARE, Map.of()), site);

        assertThat(program.hardware()).isEmpty();
        assertThat(program.warnings()).isEmpty();
    }

    @Test
    void warnsOfAPlatformItDoesNotKnowAndTakesTheProgramWithoutIt() {
        SourceProgram program = BkCatalogSourcePrograms.of(game(Map.of("Платформа", "БК0012")), site);

        assertThat(program.hardware()).isEmpty();
        assertThat(program.warnings()).containsExactly(new SourceWarning(SourceWarning.Kind.PLATFORM, "БК0012"));
    }

    @Test
    void leavesOutSoftwareForThePc() {
        assertThat(BkCatalogSourcePrograms.leftOutBy(software(Map.of("Платформа", "Windows"))))
                .contains("Windows");
        assertThat(BkCatalogSourcePrograms.leftOutBy(software(Map.of("Платформа", "MS-DOS"))))
                .contains("MS-DOS");
        assertThat(BkCatalogSourcePrograms.leftOutBy(software(Map.of("Платформа", "БК0010"))))
                .isEmpty();
        assertThat(BkCatalogSourcePrograms.leftOutBy(software(Map.of()))).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                "GAMES | Аркада | 8536e450-1fc7-5985-9cd2-97b499713a67",
                "GAMES | Логическая | b3570270-ca07-57bf-97d5-f2fbb7a7130a",
                "GAMES | Лабиринт | d1c1f13f-04d6-5ad1-82a2-88bccdb824e9",
                "GAMES | Леталка-Стрелялка | 2c8bb7d0-4b81-520e-ae28-4b135e36745e",
                "GAMES | Стрелялка | 36d8df17-2b5b-518b-ac38-c3df4913be05",
                "GAMES | Платформа | 29d0e7c6-fe95-5bd4-9339-d00f3a490c85",
                "GAMES | Драки | 816b6a8f-315e-5cc9-ae30-d9ee58f787cd",
                "GAMES | Ралли | e69d46e6-8817-5da4-81e9-c717b9712b1e",
                "GAMES | Азартная | a216cb2f-6486-5b94-8f6b-9aee9a9a477c",
                "GAMES | Стратегия | 623ec340-9a17-5999-9331-9a52e9fa6646",
                "GAMES | Квест | b5e37aaa-60ea-52fe-a587-c399dab174d5",
                "GAMES | Point-and-Click | 031316eb-1c4d-5876-8794-1389e3428a91",
                "GAMES | Спортивная | 3bf14055-79d0-5963-bbee-d14ad5324628",
                "GAMES | Обучающая | 3fac0cbb-4498-5cae-8b04-b36e08525725",
                "GAMES | Реклама | 4aa926ea-3246-54b1-b9c5-000b610ed078",
                "GAMES | Демо | 936b5821-6c89-54c5-93f0-a21ffb1580b3",
                "GAMES | Разное | e7a41ace-07a5-5cae-9234-34b7dd90ea93",
                "GAMES | Эротическая | c3d2848f-3803-5b6b-b52d-102ebd3fa715",
                "SOFTWARE | Пресса | 15c479d7-f10e-5291-8f99-83f320b2a955",
                "SOFTWARE | Графическая | 16b736cf-b7e9-51cc-9156-07fe321a922d",
                "SOFTWARE | Графический редактор | 04f76abf-3bb3-5f68-b61f-4ab0e8e760ed",
                "SOFTWARE | Редактор Спрайтов | 518de908-29b9-5a30-b5f0-a40afa35bcd6",
                "SOFTWARE | Утилита | b78ed6d4-3fb8-5bad-9c19-401d0ccbd0a5",
                "SOFTWARE | Копировщик | 9683f749-75f1-535f-8c48-e42031fe6374",
                "SOFTWARE | Отладчик | 99f97638-6c50-57aa-be30-268308de2e17",
                "SOFTWARE | Язык программирования | ccd66987-944c-5932-a6d8-87d1293f7dfa",
                "SOFTWARE | Ассемблер | d05ad688-b25b-5a85-8d2f-06a2a5dc9d4e",
                "SOFTWARE | Дизассемблер | d05ad688-b25b-5a85-8d2f-06a2a5dc9d4e",
                "SOFTWARE | Музыкальный редактор | 32543277-e739-554c-810b-755e96f35da7",
                "SOFTWARE | Синтез речи | 943cbb47-1938-5bfe-beeb-a7fcf9d20031",
                "SOFTWARE | Музыка | 850090ce-5bd1-5874-86b1-f673e97a47c7",
                "SOFTWARE | Операционная система | a540e99e-e091-55c9-88db-9dc5420de1bd",
                "SOFTWARE | Текстовый редактор | e106c954-4886-50c1-b261-4185033b4a1c",
                "SOFTWARE | Читалка | d14113cf-eee7-5bea-8949-dbac4f1da892",
                "SOFTWARE | База Данных | 474c717c-abc9-5a07-aae3-35cbe3a46ff6",
                "DEMOSCENE | demo | 936b5821-6c89-54c5-93f0-a21ffb1580b3",
                "DEMOSCENE | wild | 64ad90af-cf42-59c0-ad1f-52500c3ec926",
                "DEMOSCENE | intro | 47301d2f-eb74-5c1c-90c3-090b4f33e498",
                "DEMOSCENE | 32b | 7c70b992-b0d6-5f85-9556-6d9e90235c18",
                "DEMOSCENE | 64b | 09b75ce2-e0a0-5d89-9ae5-15c3b8a970b9",
                "DEMOSCENE | 128b | faa51702-e7ea-5b89-beb8-e54153ec41d0",
                "DEMOSCENE | 256b | b7dcf3ef-7e99-5efb-a68b-6c32505dd147",
                "DEMOSCENE | 512b | 17b7ec6a-193e-568d-8451-1908870387de",
                "DEMOSCENE | 4k | e9d495af-b0d1-5b0f-a22f-6955b423ab8d",
                "DEMOSCENE | 8k | 3a5f9338-ff27-53bc-88e5-342c9143a5b5",
                "DEMOSCENE | 16k | f998de7e-e754-5a30-8cb4-7e00a3127528",
                "DEMOSCENE | 64k | 47301d2f-eb74-5c1c-90c3-090b4f33e498",
                "DEMOSCENE | procedural graphics | 055a80fc-d3cc-5925-ad5b-d5b69628a633",
                "DEMOSCENE | invitation | e3295599-c19a-5119-a511-664c89b841f0",
                "DEMOSCENE | musicdisk | 850090ce-5bd1-5874-86b1-f673e97a47c7",
                "DEMOSCENE | demopack | 72d7e77a-f330-5a5a-923a-d0a62f8fe620",
                "DEMOSCENE | game | 081bf3a1-782b-5a8b-8d12-3b155e0a58cb",
                "DEMOSCENE | demotool | 32543277-e739-554c-810b-755e96f35da7"
            })
    void placesTheEntryInTheCategoryItsGenreStandsForInItsSection(
            BkCatalogSection section, String genre, UUID category) {
        SourceProgram program = BkCatalogSourcePrograms.of(entry(section, Map.of("Жанр", genre)), site);

        assertThat(program.categories()).containsExactly(new CategoryId(category));
        assertThat(program.warnings()).isEmpty();
    }

    @Test
    void placesAnEntryOfSeveralGenresInTheCategoryOfEach() {
        SourceProgram program = BkCatalogSourcePrograms.of(game(Map.of("Жанр", "Аркада, Стрелялка")), site);

        assertThat(program.categories())
                .containsExactlyInAnyOrder(
                        category("8536e450-1fc7-5985-9cd2-97b499713a67"),
                        category("36d8df17-2b5b-518b-ac38-c3df4913be05"));
    }

    @Test
    void placesAGameWithoutAGenreInGames() {
        assertThat(BkCatalogSourcePrograms.of(game(Map.of()), site).categories())
                .containsExactly(category("081bf3a1-782b-5a8b-8d12-3b155e0a58cb"));
    }

    @Test
    void warnsOfAGenreItDoesNotKnowAndPlacesTheEntryByTheOthers() {
        SourceProgram program = BkCatalogSourcePrograms.of(game(Map.of("Жанр", "Аркада, Симулятор")), site);

        assertThat(program.categories()).containsExactly(category("8536e450-1fc7-5985-9cd2-97b499713a67"));
        assertThat(program.warnings()).containsExactly(new SourceWarning(SourceWarning.Kind.GENRE, "Симулятор"));
    }

    @Test
    void datesTheSoftwareByItsYearOfIssue() {
        SourceProgram program =
                BkCatalogSourcePrograms.of(game(Map.of("Год выпуска", "1988", "Дата выхода", "1990")), site);

        assertThat(program.softwareDate()).isEqualTo(ReleaseDate.ofYear(Year.of(1988)));
    }

    @Test
    void datesTheSoftwareByTheYearItCameOutWithoutAYearOfIssue() {
        SourceProgram program = BkCatalogSourcePrograms.of(game(Map.of("Дата выхода", "15.01.92")), site);

        assertThat(program.softwareDate()).isEqualTo(ReleaseDate.ofYear(Year.of(1992)));
    }

    @Test
    void leavesTheSoftwareUndatedWithoutEitherDate() {
        assertThat(BkCatalogSourcePrograms.of(game(Map.of()), site).softwareDate())
                .isNull();
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                "1990 | 1990 |",
                "15.01.1992 | 1992 | 1992-01-15",
                "5.5.2025 | 2025 | 2025-05-05",
                "15.01.92 | 1992 | 1992-01-15",
                "15.08.99 | 1999 | 1999-08-15",
                "17.09.12 | 2012 | 2012-09-17",
                "26.11.26 | 2026 | 2026-11-26",
                "2021.03.07 | 2021 | 2021-03-07",
                "october 2026 | 2026 |",
                "April 2024 | 2024 |"
            })
    void datesEveryReleaseByTheDayOrTheYearTheProgramCameOut(String came, int year, LocalDate day) {
        SourceProgram program =
                BkCatalogSourcePrograms.of(game(Map.of("Дата выхода", came, "Имя файла 1", "a.zip")), site);

        assertThat(program.releases())
                .extracting(SourceRelease::date)
                .containsExactly(day == null ? ReleaseDate.ofYear(Year.of(year)) : ReleaseDate.ofDay(day));
        assertThat(program.warnings()).isEmpty();
    }

    @Test
    void leavesTheReleasesUndatedWhenTheProgramCameOutInSeveralYears() {
        SourceProgram program = BkCatalogSourcePrograms.of(
                game(Map.of("Год выпуска", "1988", "Дата выхода", "1988, 1989, 1990", "Имя файла 1", "a.zip")), site);

        assertThat(program.releases()).extracting(SourceRelease::date).containsOnlyNulls();
        assertThat(program.softwareDate()).isEqualTo(ReleaseDate.ofYear(Year.of(1988)));
        assertThat(program.warnings()).isEmpty();
    }

    @Test
    void warnsOfADateItCannotRead() {
        SourceProgram program =
                BkCatalogSourcePrograms.of(game(Map.of("Дата выхода", "весна 1991", "Имя файла 1", "a.zip")), site);

        assertThat(program.releases()).extracting(SourceRelease::date).containsOnlyNulls();
        assertThat(program.warnings()).containsExactly(new SourceWarning(SourceWarning.Kind.DATE, "весна 1991"));
    }

    @Test
    void creditsEveryNameWholeAsALabelWithTheRoleOfItsColumn() {
        SourceProgram program = BkCatalogSourcePrograms.of(
                game(Map.of(
                        "Авторы", "Мачуговский А. (Manwe/SandS), Сизых Павел",
                        "Издатель", "АКТ",
                        "Графика", "Малышев Анатолий",
                        "Музыка", "Reddie")),
                site);

        assertThat(program.authors())
                .containsExactly(
                        SourceAuthor.label(new LabelName("Мачуговский А. (Manwe/SandS)"), List.of("unknown")),
                        SourceAuthor.label(new LabelName("Сизых Павел"), List.of("unknown")),
                        SourceAuthor.label(new LabelName("АКТ"), List.of("publisher")),
                        SourceAuthor.label(new LabelName("Малышев Анатолий"), List.of("graphics")),
                        SourceAuthor.label(new LabelName("Reddie"), List.of("music")));
    }

    @Test
    void creditsANameOfSeveralColumnsOnceWithTheRoleOfEach() {
        SourceProgram program = BkCatalogSourcePrograms.of(
                game(Map.of("Авторы", "Manwe/SandS", "Графика", "Manwe/SandS", "Музыка", "Manwe/SandS")), site);

        assertThat(program.authors())
                .containsExactly(SourceAuthor.label(new LabelName("Manwe/SandS"), List.of("graphics", "music")));
    }

    @Test
    void makesEveryFileOfTheEntryAReleaseOfItsOwnWithWhatTheEntrySaysOfIt() {
        site.files.put("bk_games_files/digger.zip", ZIP);
        site.files.put("bk_games_files/digger-fixed.zip", new byte[] {'P', 'K', 3, 4, 0});
        SourceProgram program = BkCatalogSourcePrograms.of(
                game(Map.of(
                        "Имя файла 1", "digger.zip",
                        "Имя файла 2", "digger-fixed.zip",
                        "Описание файла 2", "Исправленная версия",
                        "Дата выхода", "1990")),
                site);

        assertThat(program.releases())
                .extracting(release -> Objects.requireNonNull(release.file()).fileName())
                .containsExactly("digger.zip", "digger-fixed.zip");
        assertThat(program.releases())
                .extracting(SourceRelease::description)
                .containsExactly(null, new ReleaseDescription("Исправленная версия"));
        assertThat(program.releases()).extracting(SourceRelease::date).containsOnly(ReleaseDate.ofYear(Year.of(1990)));
        assertThat(program.releases())
                .extracting(release -> Objects.requireNonNull(release.file()).size())
                .containsExactly(4L, 5L);
        assertThat(program.warnings()).isEmpty();
    }

    @Test
    void readsTheFilesOfProgramsAndOfTheDemosceneFromTheirFolder() {
        site.files.put("bk_files/ships-11m.zip", ZIP);
        SourceProgram program = BkCatalogSourcePrograms.of(
                entry(BkCatalogSection.DEMOSCENE, Map.of("Имя файла 1", "ships-11m.zip")), site);

        assertThat(program.releases()).extracting(SourceRelease::file).doesNotContainNull();
        assertThat(site.downloaded).containsExactly("bk_files/ships-11m.zip");
    }

    @Test
    void warnsOfAFileTheRepositoryLacksAndKeepsItsReleaseWithoutIt() {
        SourceProgram program = BkCatalogSourcePrograms.of(
                game(Map.of("Имя файла 1", "lost.zip", "Описание файла 1", "Кассетная версия")), site);

        assertThat(program.releases())
                .containsExactly(new SourceRelease(new ReleaseDescription("Кассетная версия"), null, Set.of(), null));
        assertThat(program.warnings()).containsExactly(new SourceWarning(SourceWarning.Kind.FILE_MISSING, "lost.zip"));
    }

    @Test
    void takesTheFullSizeScreenshotsInTheirOrderAndWarnsOfOneTheRepositoryLacks() {
        site.files.put("bk_games_screenshots/digger1.png", PNG);
        site.files.put("bk_games_screenshots/digger3.png", BIGGER_PNG);
        SourceProgram program = BkCatalogSourcePrograms.of(
                game(Map.of("Скриншот 1", "digger1.png", "Скриншот 2", "digger2.png", "Скриншот 3", "digger3.png")),
                site);

        assertThat(program.screenshots())
                .extracting(image -> image.name().value())
                .containsExactly("digger1.png", "digger3.png");
        assertThat(program.warnings())
                .containsExactly(new SourceWarning(SourceWarning.Kind.SCREENSHOT_MISSING, "digger2.png"));
    }

    @Test
    void linksToThePageOfTheEntryItsRepositoryAndItsVideo() {
        SourceProgram program = BkCatalogSourcePrograms.of(
                demo(Map.of(
                        "Ссылка на страницу", "https://www.pouet.net/prod.php?which=106707",
                        "ГитХаб", "https://github.com/blairecas/baby2_bk/",
                        "Видео", "https://youtu.be/xyz")),
                site);

        assertThat(program.links())
                .containsExactly(
                        link(SoftwareLinkType.GENERAL, "https://www.pouet.net/prod.php?which=106707"),
                        link(SoftwareLinkType.HOMEPAGE, "https://github.com/blairecas/baby2_bk/"),
                        link(SoftwareLinkType.YOUTUBE, "https://youtu.be/xyz"));
    }

    @Test
    void linksToAPageThatIsAlsoTheRepositoryOnceAsTheRepository() {
        SourceProgram program = BkCatalogSourcePrograms.of(
                demo(Map.of(
                        "Ссылка на страницу", "https://github.com/prcoder-1/zak-bk0010",
                        "ГитХаб", "https://github.com/prcoder-1/zak-bk0010")),
                site);

        assertThat(program.links())
                .containsExactly(link(SoftwareLinkType.HOMEPAGE, "https://github.com/prcoder-1/zak-bk0010"));
    }

    @Test
    void doesNotLinkToTheCatalogItselfNorToWhatIsNoAddress() {
        SourceProgram program = BkCatalogSourcePrograms.of(
                demo(Map.of("Ссылка на страницу", "https://kalininskiy.github.io/bk-catalog/", "ГитХаб", "нет")), site);

        assertThat(program.links()).isEmpty();
    }

    @Test
    void tellsWhereAPortComesFrom() {
        SourceProgram program = BkCatalogSourcePrograms.of(
                game(Map.of(
                        "Происхождение", "Порт с изменениями",
                        "Достоверность происхождения", "Достоверно",
                        "Характер переработки", "Использованы материалы оригинала, Изменены графика и звук",
                        "Связанные игры (первоисточник)", "Artillery (The Portland IBM Personal Computer Club, 1982)",
                        "Связанные игры (ссылка)", "https://www.mobygames.com/game/72746/artillery/",
                        "Связанные игры (Платформа)", "IBM PC (MS-DOS)")),
                site);

        assertThat(program.lineage())
                .isEqualTo(new SoftwareLineage(
                        LineageKind.PORT_WITH_CHANGES,
                        LineageReliability.RELIABLE,
                        Set.of(LineageRework.ORIGINAL_MATERIAL_USED, LineageRework.GRAPHICS_AND_SOUND_CHANGED),
                        new LineageSource("Artillery (The Portland IBM Personal Computer Club, 1982), IBM PC (MS-DOS),"
                                + " https://www.mobygames.com/game/72746/artillery/")));
        assertThat(program.warnings()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"Оригинал, ORIGINAL", "Порт, PORT", "Ремейк, REMAKE", "Порт с изменениями, PORT_WITH_CHANGES"})
    void takesTheKindOfTheLineage(String kind, LineageKind lineage) {
        SourceProgram program = BkCatalogSourcePrograms.of(game(Map.of("Происхождение", kind)), site);

        assertThat(program.lineage()).isEqualTo(new SoftwareLineage(lineage, null, Set.of(), null));
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                "Без существенных изменений | NO_SUBSTANTIAL_CHANGES",
                "Использованы материалы оригинала | ORIGINAL_MATERIAL_USED",
                "Добавлены новые уровни | NEW_LEVELS_ADDED",
                "Изменены графика и звук | GRAPHICS_AND_SOUND_CHANGED",
                "Полностью новая реализация | NEW_IMPLEMENTATION"
            })
    void takesTheNatureOfTheRework(String rework, LineageRework lineage) {
        SourceProgram program =
                BkCatalogSourcePrograms.of(game(Map.of("Происхождение", "Порт", "Характер переработки", rework)), site);

        assertThat(program.lineage().rework()).containsExactly(lineage);
    }

    @Test
    void namesAGameOfTheCatalogItselfAsTheOriginalByTheAddressOfItsCard() {
        SourceProgram program = BkCatalogSourcePrograms.of(
                game(Map.of(
                        "Происхождение", "Ремейк",
                        "Связанные игры (первоисточник)", "BABY (1989)",
                        "Связанные игры (ссылка)", "319",
                        "Связанные игры (Платформа)", "БК0010")),
                site);

        assertThat(program.lineage().source())
                .isEqualTo(
                        new LineageSource("BABY (1989), БК0010, https://kalininskiy.github.io/bk-catalog/#game-319"));
    }

    @Test
    void warnsOfAPartOfTheLineageItDoesNotKnowAndTakesTheRest() {
        SourceProgram program = BkCatalogSourcePrograms.of(
                game(Map.of(
                        "Происхождение", "Порт",
                        "Достоверность происхождения", "Предположительно",
                        "Характер переработки", "Добавлен звук, Добавлены новые уровни")),
                site);

        assertThat(program.lineage())
                .isEqualTo(new SoftwareLineage(LineageKind.PORT, null, Set.of(LineageRework.NEW_LEVELS_ADDED), null));
        assertThat(program.warnings())
                .containsExactly(
                        new SourceWarning(SourceWarning.Kind.LINEAGE, "Предположительно"),
                        new SourceWarning(SourceWarning.Kind.LINEAGE, "Добавлен звук"));
    }

    @Test
    void leavesTheLineageUnknownWhenTheKindIsOneItDoesNotKnow() {
        SourceProgram program = BkCatalogSourcePrograms.of(
                game(Map.of(
                        "Происхождение", "Конверсия",
                        "Достоверность происхождения", "Достоверно",
                        "Связанные игры (первоисточник)", "Elite")),
                site);

        assertThat(program.lineage()).isEqualTo(new SoftwareLineage(null, null, Set.of(), new LineageSource("Elite")));
        assertThat(program.warnings()).containsExactly(new SourceWarning(SourceWarning.Kind.LINEAGE, "Конверсия"));
    }

    @Test
    void warnsOfAnOriginalTooLongToKeepAndTakesTheLineageWithoutIt() {
        String title = "a".repeat(300);
        SourceProgram program = BkCatalogSourcePrograms.of(
                game(Map.of("Происхождение", "Порт", "Связанные игры (первоисточник)", title)), site);

        assertThat(program.lineage()).isEqualTo(new SoftwareLineage(LineageKind.PORT, null, Set.of(), null));
        assertThat(program.warnings())
                .containsExactly(new SourceWarning(SourceWarning.Kind.LINEAGE_SOURCE_TOO_LONG, title));
    }

    private static BkCatalogEntry game(Map<String, String> columns) {
        return entry(BkCatalogSection.GAMES, columns);
    }

    private static BkCatalogEntry software(Map<String, String> columns) {
        return entry(BkCatalogSection.SOFTWARE, columns);
    }

    private static BkCatalogEntry demo(Map<String, String> columns) {
        return entry(BkCatalogSection.DEMOSCENE, columns);
    }

    /** An entry of the section with the columns given, the others empty; a title and an id when the test gives none. */
    private static BkCatalogEntry entry(BkCatalogSection section, Map<String, String> columns) {
        Map<String, String> all = new HashMap<>(Map.of("ID", "1", "Название", "Digger"));
        all.putAll(columns);
        return new BkCatalogEntry(section, all);
    }

    private static CategoryId category(String id) {
        return new CategoryId(UUID.fromString(id));
    }

    private static SoftwareLink link(SoftwareLinkType type, String url) {
        return new SoftwareLink(type, new SoftwareUrl(url));
    }

    /** The files of the repository the test puts there; it records what was downloaded, in order. */
    private static final class Site implements BkCatalogSite {
        private final Map<String, byte[]> files = new HashMap<>();
        private final List<String> downloaded = new ArrayList<>();

        @Override
        public List<BkCatalogEntry> entries(BkCatalogSection section) {
            return List.of();
        }

        @Override
        public Optional<byte[]> download(String path) {
            downloaded.add(path);
            return Optional.ofNullable(files.get(path));
        }
    }
}
