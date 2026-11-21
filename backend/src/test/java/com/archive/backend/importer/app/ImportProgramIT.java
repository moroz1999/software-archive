package com.archive.backend.importer.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.author.app.CreateAuthorCommand;
import com.archive.backend.author.app.CreateAuthorHandler;
import com.archive.backend.author.app.DeleteAuthorCommand;
import com.archive.backend.author.app.DeleteAuthorHandler;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.RealName;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.group.app.CreateGroupCommand;
import com.archive.backend.group.app.CreateGroupHandler;
import com.archive.backend.group.app.DeleteGroupCommand;
import com.archive.backend.group.app.DeleteGroupHandler;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.Importer;
import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import com.archive.backend.importer.domain.run.ImportScope;
import com.archive.backend.importer.domain.source.SourceAuthor;
import com.archive.backend.importer.domain.source.SourceAuthorKind;
import com.archive.backend.importer.domain.source.SourcePlace;
import com.archive.backend.importer.domain.source.SourcePlaceKind;
import com.archive.backend.importer.domain.source.SourceProgram;
import com.archive.backend.importer.read.ImportRunAuthorEntry;
import com.archive.backend.importer.read.ImportRunReadRepository;
import com.archive.backend.importer.read.ImportRunReport;
import com.archive.backend.label.app.MakeAuthorOfLabelCommand;
import com.archive.backend.label.app.MakeAuthorOfLabelHandler;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.UnmarkedLabelDraft;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.software.app.CreateReleaseCommand;
import com.archive.backend.software.app.CreateReleaseHandler;
import com.archive.backend.software.app.CreateSoftwareCommand;
import com.archive.backend.software.app.CreateSoftwareHandler;
import com.archive.backend.software.app.DeleteSoftwareCommand;
import com.archive.backend.software.app.DeleteSoftwareHandler;
import com.archive.backend.software.domain.PreservationStatus;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.ScreenshotFileName;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.user.TestActors;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.Year;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The one import every source goes through (docs/domain/imports.md), against programs as a parser hands them over;
 * every program commits for real.
 */
@IntegrationTest
class ImportProgramIT {

    private static final String ARCHIVE = "test-archive";
    private static final PlatformCode VECTOR = new PlatformCode("vector");
    private static final HardwareCode VECTOR_06C = new HardwareCode("vector06c");
    private static final HardwareCode KRISTA_2 = new HardwareCode("krista2");
    private static final HardwareCode TAPE = new HardwareCode("tape");
    private static final CategoryId ARCADE = new CategoryId(UUID.fromString("8536e450-1fc7-5985-9cd2-97b499713a67"));
    private static final UUID UNKNOWN_COUNTRY = UUID.fromString("00000000-0000-0000-0000-00000000c001");
    private static final UUID KNOWN_CITY = UUID.fromString("00000000-0000-0000-00c1-000000000001");
    private static final byte[] ELITE_ROM = "elite program".getBytes(StandardCharsets.UTF_8);
    private static final byte[] EXTRA_ROM = "extra program".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0};

    @Autowired
    private ImportProgramHandler handler;

    @Autowired
    private CreateSoftwareHandler createSoftware;

    @Autowired
    private CreateReleaseHandler createRelease;

    @Autowired
    private MakeAuthorOfLabelHandler makeAuthor;

    @Autowired
    private DeleteAuthorHandler deleteAuthor;

    @Autowired
    private DeleteSoftwareHandler deleteSoftware;

    @Autowired
    private DeleteGroupHandler deleteGroup;

    @Autowired
    private CreateAuthorHandler createAuthor;

    @Autowired
    private CreateGroupHandler createGroup;

    @Autowired
    private ImportRunRepository runs;

    @Autowired
    private ImportRunReadRepository reports;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void knownCity() {
        TestActors.store(jdbc);
        cleanUp();
        jdbc.update("INSERT INTO city (id, country_id) VALUES (?, ?)", KNOWN_CITY, UNKNOWN_COUNTRY);
        jdbc.update(
                "INSERT INTO city_name (city_id, language, name) VALUES (?, 'en', 'Testgrad'), (?, 'ru', 'Тестоград')",
                KNOWN_CITY,
                KNOWN_CITY);
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM software WHERE added_by IN (?, ?)", Importer.ID.value(), TestActors.ADMIN_ID.value());
        // What the import added, and what the archive had before it: people and groups an administrator added.
        for (UUID adder : List.of(Importer.ID.value(), TestActors.ADMIN_ID.value())) {
            jdbc.update("DELETE FROM label WHERE added_by = ?", adder);
            jdbc.update(
                    "DELETE FROM entity_location WHERE entity_id IN (SELECT id FROM person WHERE added_by = ?)"
                            + " OR entity_id IN (SELECT id FROM \"group\" WHERE added_by = ?)",
                    adder,
                    adder);
            jdbc.update("DELETE FROM person WHERE added_by = ?", adder);
            jdbc.update("DELETE FROM \"group\" WHERE added_by = ?", adder);
        }
        jdbc.update("DELETE FROM external_origin");
        jdbc.update("DELETE FROM city WHERE country_id = ?", UNKNOWN_COUNTRY);
        jdbc.update("DELETE FROM country WHERE id IN (SELECT country_id FROM country_name WHERE name = 'Атлантида')");
        jdbc.update("DELETE FROM import_run");
    }

    @Test
    void addsANewProgramWithItsReleaseScreenshotsAuthorsAndOrigin() {
        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .description("Theirs")
                .hardware(KRISTA_2)
                .releaseHardware(TAPE)
                .categories(ARCADE)
                .authors(SourceAuthor.label(new LabelName("IVAN A")))
                .screenshots(screenshot("elite.png", 100))
                .build());

        UUID software = software("Elite");
        assertThat(jdbc.queryForObject("SELECT description FROM software WHERE id = ?", String.class, software))
                .isEqualTo("Theirs");
        assertThat(jdbc.queryForList(
                        "SELECT hardware_code FROM software_hardware WHERE software_id = ?", String.class, software))
                .containsExactly("krista2");
        assertThat(jdbc.queryForList(
                        "SELECT h.hardware_code FROM software_release_hardware h"
                                + " JOIN software_release r ON r.id = h.release_id WHERE r.software_id = ?",
                        String.class,
                        software))
                .containsExactly("tape");
        assertThat(jdbc.queryForList(
                        "SELECT category_id FROM software_category WHERE software_id = ?", UUID.class, software))
                .containsExactly(ARCADE.value());
        assertThat(screenshots(software)).containsExactly("elite.png:100");
        assertThat(labelCredits(software)).containsExactly("IVAN A");
        assertThat(origin("SOFTWARE", "1")).isEqualTo(software);
        assertThat(lastReport().added()).isOne();
    }

    @Test
    void takesAProgramWhoseProgramFilesTheArchiveHasAsTheSameSoftwareAndAddsWhatItLacks() {
        SoftwareId ours = ourSoftware("Elite", zip(Map.of("ELITE.ROM", ELITE_ROM, "readme.txt", bytes("Read me"))));

        importProgram(program("1", "Elite (other title)", zip(Map.of("games/elite.rom", ELITE_ROM)))
                .description("Theirs")
                .categories(ARCADE)
                .authors(SourceAuthor.label(new LabelName("IVAN A")))
                .build());

        assertThat(jdbc.queryForObject("SELECT count(*) FROM software WHERE title LIKE 'Elite%'", Integer.class))
                .isOne();
        assertThat(jdbc.queryForMap("SELECT title, description FROM software WHERE id = ?", ours.value()))
                .containsEntry("title", "Elite")
                .containsEntry("description", "Theirs");
        assertThat(jdbc.queryForList(
                        "SELECT category_id FROM software_category WHERE software_id = ?", UUID.class, ours.value()))
                .containsExactly(ARCADE.value());
        assertThat(labelCredits(ours.value())).containsExactly("IVAN A");
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software_release WHERE software_id = ?", Integer.class, ours.value()))
                .isOne();
        assertThat(origin("SOFTWARE", "1")).isEqualTo(ours.value());
        ImportRunReport report = lastReport();
        assertThat(List.of(report.added(), report.known())).containsExactly(0, 1);
        assertThat(report.knownPrograms()).singleElement().satisfies(known -> {
            assertThat(known.externalId()).isEqualTo("1");
            assertThat(known.softwareId()).isEqualTo(ours.value());
            assertThat(known.title()).isEqualTo("Elite (other title)");
            assertThat(known.exists()).isTrue();
            assertThat(known.supplements())
                    .containsExactlyInAnyOrder("description", "release-date", "categories", "credits");
        });
    }

    @Test
    void reportsAKnownProgramThatLackedNothingAsSuch() {
        SourceProgram elite = program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .description("Theirs")
                .screenshots(screenshot("elite.png", 100))
                .build();
        importProgram(elite);

        importProgram(elite);

        assertThat(lastReport().knownPrograms())
                .singleElement()
                .satisfies(known -> assertThat(known.supplements()).isEmpty());
    }

    @Test
    void reportsTheScreenshotsAddedToAKnownProgram() {
        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM))).build());

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .screenshots(screenshot("elite.png", 100))
                .build());

        assertThat(lastReport().knownPrograms())
                .singleElement()
                .satisfies(known -> assertThat(known.supplements()).containsExactly("screenshots"));
    }

    @Test
    void reportsTheAuthorsAndPlacesItCreatedForAProgram() {
        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(
                        SourceAuthor.label(new LabelName("IVAN A")),
                        person(
                                "17",
                                "IVAN",
                                "Иван Андреев",
                                new SourcePlace("Новоград", Language.RU, SourcePlaceKind.CITY)),
                        new SourceAuthor(
                                "18",
                                new LabelName("Fenomen"),
                                SourceAuthorKind.GROUP,
                                null,
                                new SourcePlace("Атлантида", Language.RU, SourcePlaceKind.COUNTRY)))
                .build());

        ImportRunReport report = lastReport();
        assertThat(report.authors())
                .extracting(
                        author -> author.kind() + ":" + author.name() + ":" + author.created() + ":" + author.exists())
                .containsExactly("label:IVAN A:true:true", "author:IVAN:true:true", "group:Fenomen:true:true");
        assertThat(report.authors())
                .extracting(ImportRunAuthorEntry::id)
                .containsExactly(
                        jdbc.queryForObject("SELECT id FROM label WHERE name = 'IVAN A'", UUID.class),
                        origin("AUTHOR", "17"),
                        origin("GROUP", "18"));
        assertThat(report.places())
                .extracting(place -> place.kind() + ":" + place.name())
                .containsExactly("city:Новоград", "country:Атлантида");
    }

    @Test
    void reportsTheAuthorsFoundInTheArchiveOnlyTheFirstTime() {
        UUID ours = ourPerson("Иван Андреев", "IVAN", KNOWN_CITY);
        SourceAuthor ivan =
                person("17", "IVAN", "Иван Андреев", new SourcePlace("Testgrad", Language.EN, SourcePlaceKind.CITY));

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(ivan)
                .build());

        ImportRunReport first = lastReport();
        assertThat(first.authors()).singleElement().satisfies(author -> {
            assertThat(author.kind()).isEqualTo("author");
            assertThat(author.id()).isEqualTo(ours);
            assertThat(author.name()).isEqualTo("IVAN");
            assertThat(author.created()).isFalse();
        });
        assertThat(first.places()).isEmpty();

        importProgram(program("2", "Exolon", zip(Map.of("EXOLON.ROM", EXTRA_ROM)))
                .authors(ivan)
                .build());

        assertThat(lastReport().authors()).isEmpty();
    }

    @Test
    void takesThePreservationStatusOfTheSourceForANewProgramAndForAKnownOneWhileItIsUnknown() {
        SoftwareId unknown = ourSoftware("Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)));
        SoftwareId inSales = ourSoftware("Zork", zip(Map.of("ZORK.ROM", bytes("Zork"))));
        jdbc.update("UPDATE software SET preservation = 'in-sales' WHERE id = ?", inSales.value());

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .preservation(PreservationStatus.MISSING_IN_ACTION)
                .build());
        importProgram(program("2", "Zork", zip(Map.of("ZORK.ROM", bytes("Zork"))))
                .preservation(PreservationStatus.MISSING_IN_ACTION)
                .build());
        importProgram(program("3", "Nature", zip(Map.of("NATURE.ROM", bytes("Nature"))))
                .preservation(PreservationStatus.MISSING_IN_ACTION)
                .build());

        assertThat(preservation(unknown.value())).isEqualTo("missing-in-action");
        assertThat(preservation(inSales.value())).isEqualTo("in-sales");
        assertThat(preservation(software("Nature"))).isEqualTo("missing-in-action");
    }

    @Test
    void addsAsAnotherProgramOneWithAProgramFileMoreThanTheArchiveHas() {
        SoftwareId ours = ourSoftware("Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)));

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM, "EXTRA.ROM", EXTRA_ROM)))
                .build());

        assertThat(jdbc.queryForList("SELECT id FROM software WHERE title = 'Elite'", UUID.class))
                .hasSize(2)
                .contains(ours.value());
        assertThat(lastReport().added()).isOne();
    }

    @Test
    void keepsTwoEntriesOfOneSourceWithTheSameFilesTwoPrograms() {
        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM))).build());
        importProgram(program("2", "Elite (copy)", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .build());

        assertThat(origin("SOFTWARE", "1")).isNotEqualTo(origin("SOFTWARE", "2"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software WHERE title LIKE 'Elite%'", Integer.class))
                .isEqualTo(2);
    }

    @Test
    void forgetsWhereADeletedPersonOrGroupCameFromAndTakesThemAgainAsNew() {
        SourceAuthor person = new SourceAuthor(
                "17",
                new LabelName("IVAN"),
                SourceAuthorKind.PERSON,
                new RealName("Иван Андреев"),
                new SourcePlace("Testgrad", Language.EN, SourcePlaceKind.CITY));
        SourceAuthor group = new SourceAuthor("18", new LabelName("Fenomen"), SourceAuthorKind.GROUP, null, null);
        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(person, group)
                .build());
        UUID deletedPerson = origin("AUTHOR", "17");
        UUID deletedGroup = origin("GROUP", "18");

        deleteAuthor.handle(new DeleteAuthorCommand(TestActors.ADMIN, new AuthorId(deletedPerson)));
        deleteGroup.handle(new DeleteGroupCommand(TestActors.ADMIN, new GroupId(deletedGroup)));

        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM external_origin WHERE entity_id IN (?, ?)",
                        Integer.class,
                        deletedPerson,
                        deletedGroup))
                .isZero();
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM entity_location WHERE entity_id = ?", Integer.class, deletedPerson))
                .isZero();
        assertThat(software("Elite")).isNotNull();
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software_release WHERE software_id = ?",
                        Integer.class,
                        software("Elite")))
                .isOne();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM city WHERE id = ?", Integer.class, KNOWN_CITY))
                .isOne();
        importProgram(program("2", "Exolon", zip(Map.of("EXOLON.ROM", EXTRA_ROM)))
                .authors(person, group)
                .build());
        assertThat(origin("AUTHOR", "17")).isNotEqualTo(deletedPerson);
        assertThat(origin("GROUP", "18")).isNotEqualTo(deletedGroup);
    }

    @Test
    void deletingSoftwareForgetsOnlyTheSoftwareAndKeepsItsAuthors() {
        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(
                        SourceAuthor.label(new LabelName("IVAN A")),
                        new SourceAuthor("18", new LabelName("Fenomen"), SourceAuthorKind.GROUP, null, null))
                .build());
        UUID software = software("Elite");

        deleteSoftware.handle(new DeleteSoftwareCommand(TestActors.ADMIN, new SoftwareId(software)));

        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM release_file f JOIN software_release r ON r.id = f.release_id"
                                + " WHERE r.software_id = ?",
                        Integer.class,
                        software))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM external_origin WHERE kind = 'SOFTWARE'", Integer.class))
                .isZero();
        assertThat(jdbc.queryForList("SELECT kind FROM external_origin ORDER BY kind", String.class))
                .containsExactly("GROUP", "LABEL");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM label WHERE name = 'IVAN A'", Integer.class))
                .isOne();
    }

    @Test
    void addsAsAnotherProgramOneWithoutProgramFiles() {
        ourSoftware("Elite", zip(Map.of("readme.txt", bytes("Read me"))));

        importProgram(program("1", "Elite", zip(Map.of("readme.txt", bytes("Read me"))))
                .build());

        assertThat(jdbc.queryForObject("SELECT count(*) FROM software WHERE title = 'Elite'", Integer.class))
                .isEqualTo(2);
    }

    @Test
    void addsOnAnotherRunWhatTheSourceGotSinceAndKeepsWhatTheArchiveHas() {
        byte[] file = zip(Map.of("ELITE.ROM", ELITE_ROM));
        importProgram(program("1", "Elite", file)
                .description("Theirs")
                .authors(SourceAuthor.label(new LabelName("IVAN A")))
                .screenshots(screenshot("one.png", 100))
                .build());
        UUID software = software("Elite");
        jdbc.update("UPDATE software SET description = 'Ours' WHERE id = ?", software);

        importProgram(program("1", "Elite", file)
                .description("Theirs, changed")
                .authors(SourceAuthor.label(new LabelName("IVAN A")), SourceAuthor.label(new LabelName("ZORK")))
                .screenshots(screenshot("one-again.png", 100), screenshot("two.png", 200))
                .build());

        assertThat(jdbc.queryForObject("SELECT description FROM software WHERE id = ?", String.class, software))
                .isEqualTo("Ours");
        assertThat(labelCredits(software)).containsExactly("IVAN A", "ZORK");
        assertThat(screenshots(software)).containsExactlyInAnyOrder("one.png:100", "two.png:200");
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software_release WHERE software_id = ?", Integer.class, software))
                .isOne();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM label WHERE name = 'IVAN A'", Integer.class))
                .isOne();
    }

    @Test
    void creditsThePersonAnImportedLabelWasMadeInto() {
        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(SourceAuthor.label(new LabelName("IVAN A")))
                .build());
        UUID label = jdbc.queryForObject("SELECT id FROM label WHERE name = 'IVAN A'", UUID.class);
        UUID person = makeAuthor
                .handle(new MakeAuthorOfLabelCommand(TestActors.ADMIN, new LabelId(label)))
                .value();

        importProgram(program("2", "Exolon", zip(Map.of("EXOLON.ROM", EXTRA_ROM)))
                .authors(SourceAuthor.label(new LabelName("IVAN A")))
                .build());

        assertThat(jdbc.queryForObject(
                        "SELECT person_id FROM software_contribution WHERE software_id = ?",
                        UUID.class,
                        software("Exolon")))
                .isEqualTo(person);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM label WHERE name = 'IVAN A'", Integer.class))
                .isZero();
    }

    @Test
    void makesAPersonOfAnAuthorWithARealNameFromACityFoundByItsNameInAnyLanguage() {
        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(new SourceAuthor(
                        "17",
                        new LabelName("IVAN"),
                        SourceAuthorKind.PERSON,
                        new RealName("Иван Андреев"),
                        new SourcePlace("Testgrad", Language.RU, SourcePlaceKind.CITY)))
                .build());

        UUID person = origin("AUTHOR", "17");
        assertThat(jdbc.queryForObject("SELECT real_name FROM person WHERE id = ?", String.class, person))
                .isEqualTo("Иван Андреев");
        assertThat(jdbc.queryForObject("SELECT person_id FROM label WHERE name = 'IVAN'", UUID.class))
                .isEqualTo(person);
        assertThat(cityOf(person)).isEqualTo(KNOWN_CITY);
    }

    @Test
    void creditsAPersonUnderTheNicknameTheProgramCameOutUnderEachTime() {
        SourceAuthor ivan = new SourceAuthor(
                "17", new LabelName("IVAN"), SourceAuthorKind.PERSON, new RealName("Иван Андреев"), null);
        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(ivan)
                .build());
        importProgram(program("2", "Exolon", zip(Map.of("EXOLON.ROM", EXTRA_ROM)))
                .authors(ivan)
                .build());

        UUID nickname = jdbc.queryForObject("SELECT id FROM label WHERE name = 'IVAN'", UUID.class);
        for (String title : List.of("Elite", "Exolon")) {
            assertThat(jdbc.queryForMap(
                            "SELECT person_id, label_id FROM software_contribution WHERE software_id = ?",
                            software(title)))
                    .containsEntry("person_id", null)
                    .containsEntry("label_id", nickname);
        }
    }

    @Test
    void creditsThePersonItselfOnceTheNicknameIsNoLongerTheirs() {
        SourceAuthor ivan = new SourceAuthor(
                "17", new LabelName("IVAN"), SourceAuthorKind.PERSON, new RealName("Иван Андреев"), null);
        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(ivan)
                .build());
        UUID person = origin("AUTHOR", "17");
        jdbc.update("UPDATE label SET name = 'IVAN THE GREAT' WHERE person_id = ?", person);

        importProgram(program("2", "Exolon", zip(Map.of("EXOLON.ROM", EXTRA_ROM)))
                .authors(ivan)
                .build());

        assertThat(jdbc.queryForObject(
                        "SELECT person_id FROM software_contribution WHERE software_id = ?",
                        UUID.class,
                        software("Exolon")))
                .isEqualTo(person);
    }

    @Test
    void createsACityTheArchiveDoesNotHaveInTheCountryUnknownNamedInTheLanguageOfTheSource() {
        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(new SourceAuthor(
                        "17",
                        new LabelName("IVAN"),
                        SourceAuthorKind.PERSON,
                        new RealName("Иван Андреев"),
                        new SourcePlace("Новоград", Language.RU, SourcePlaceKind.CITY)))
                .build());

        UUID city = cityOf(origin("AUTHOR", "17"));
        assertThat(jdbc.queryForObject("SELECT country_id FROM city WHERE id = ?", UUID.class, city))
                .isEqualTo(UNKNOWN_COUNTRY);
        assertThat(jdbc.queryForList(
                        "SELECT language || ':' || name FROM city_name WHERE city_id = ?", String.class, city))
                .containsExactly("ru:Новоград");
    }

    @Test
    void makesAGroupWithoutTypesOfAnAuthorTheSourceTellsIsAGroup() {
        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(new SourceAuthor("18", new LabelName("Fenomen"), SourceAuthorKind.GROUP, null, null))
                .build());

        UUID group = origin("GROUP", "18");
        assertThat(jdbc.queryForObject("SELECT name FROM \"group\" WHERE id = ?", String.class, group))
                .isEqualTo("Fenomen");
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM group_group_type WHERE group_id = ?", Integer.class, group))
                .isZero();
        assertThat(jdbc.queryForObject(
                        "SELECT group_id FROM software_contribution WHERE software_id = ?",
                        UUID.class,
                        software("Elite")))
                .isEqualTo(group);
    }

    @Test
    void fillsOnlyTheEmptyFieldsOfAnAuthorItKnows() {
        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(new SourceAuthor("17", new LabelName("IVAN"), SourceAuthorKind.PERSON, null, null))
                .build());
        importProgram(program("2", "Exolon", zip(Map.of("EXOLON.ROM", EXTRA_ROM)))
                .authors(new SourceAuthor(
                        "17",
                        new LabelName("IVAN"),
                        SourceAuthorKind.PERSON,
                        new RealName("Иван Андреев"),
                        new SourcePlace("Testgrad", Language.EN, SourcePlaceKind.CITY)))
                .build());
        importProgram(program("3", "Xevious", zip(Map.of("XEVIOUS.ROM", bytes("xevious"))))
                .authors(new SourceAuthor(
                        "17",
                        new LabelName("IVAN"),
                        SourceAuthorKind.PERSON,
                        new RealName("Иван Другой"),
                        new SourcePlace("Новоград", Language.RU, SourcePlaceKind.CITY)))
                .build());

        UUID person = origin("AUTHOR", "17");
        assertThat(jdbc.queryForObject("SELECT real_name FROM person WHERE id = ?", String.class, person))
                .isEqualTo("Иван Андреев");
        assertThat(cityOf(person)).isEqualTo(KNOWN_CITY);
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software_contribution c JOIN label l ON l.id = c.label_id"
                                + " WHERE l.person_id = ? AND l.name = 'IVAN'",
                        Integer.class,
                        person))
                .isEqualTo(3);
    }

    @Test
    void takesForThePersonOfTheArchiveAPersonOfTheSameNicknameAndFullRealName() {
        UUID ours = ourPerson("Иван Андреев", "IVAN", KNOWN_CITY);

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(person(
                        "17", "IVAN", "Иван Андреев", new SourcePlace("Testgrad", Language.EN, SourcePlaceKind.CITY)))
                .build());

        assertThat(origin("AUTHOR", "17")).isEqualTo(ours);
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM person WHERE added_by = ?", Integer.class, Importer.ID.value()))
                .isZero();
        assertThat(jdbc.queryForObject(
                        "SELECT label_id FROM software_contribution WHERE software_id = ?",
                        UUID.class,
                        software("Elite")))
                .isEqualTo(labelOf(ours, "IVAN"));
    }

    @Test
    void comparesTheNicknameAndTheRealNameOfAPersonWhateverTheCase() {
        UUID ours = ourPerson("Иван Андреев", "IVAN", null);

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(person("17", "ivan", "ИВАН АНДРЕЕВ", null))
                .build());

        assertThat(origin("AUTHOR", "17")).isEqualTo(ours);
    }

    @Test
    void makesANewPersonOfOneWhoseNicknameAloneIsThatOfAPersonOfTheArchive() {
        UUID ours = ourPerson("Иван Андреев", "IVAN", null);

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(
                        new SourceAuthor("17", new LabelName("IVAN"), SourceAuthorKind.PERSON, null, null),
                        person("18", "IVAN", "Иван Другой", null))
                .build());

        assertThat(origin("AUTHOR", "17")).isNotEqualTo(ours);
        assertThat(origin("AUTHOR", "18")).isNotEqualTo(ours).isNotEqualTo(origin("AUTHOR", "17"));
    }

    @Test
    void makesANewPersonOfOneWhoseRealNameIsNotFull() {
        UUID ours = ourPerson("Иван", "IVAN", null);

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(person("17", "IVAN", "Иван", null))
                .build());

        assertThat(origin("AUTHOR", "17")).isNotEqualTo(ours);
    }

    @Test
    void makesANewPersonOfOneFromAnotherPlace() {
        UUID ours = ourPerson("Иван Андреев", "IVAN", KNOWN_CITY);

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(person(
                        "17", "IVAN", "Иван Андреев", new SourcePlace("Новоград", Language.RU, SourcePlaceKind.CITY)))
                .build());

        assertThat(origin("AUTHOR", "17")).isNotEqualTo(ours);
    }

    @Test
    void makesANewPersonOfOneTwoPeopleOfTheArchiveMatch() {
        UUID one = ourPerson("Иван Андреев", "IVAN", null);
        UUID other = ourPerson("Иван Андреев", "IVAN", null);

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(person("17", "IVAN", "Иван Андреев", null))
                .build());

        assertThat(origin("AUTHOR", "17")).isNotIn(one, other);
    }

    @Test
    void neverTakesALabelOfASourceForAPersonOfTheArchive() {
        UUID ours = ourPerson("Иван Андреев", "IVAN", null);

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(SourceAuthor.label(new LabelName("IVAN")))
                .build());

        assertThat(jdbc.queryForObject(
                        "SELECT label_id FROM software_contribution WHERE software_id = ?",
                        UUID.class,
                        software("Elite")))
                .isNotEqualTo(labelOf(ours, "IVAN"));
    }

    @Test
    void takesForTheGroupOfTheArchiveAGroupOfTheSameNameWhateverItsCaseAndMarks() {
        UUID ours = ourGroup("EBO Soft", List.of(), KNOWN_CITY);

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(new SourceAuthor(
                        "18",
                        new LabelName("e.b.o. soft"),
                        SourceAuthorKind.GROUP,
                        null,
                        new SourcePlace("Testgrad", Language.EN, SourcePlaceKind.CITY)))
                .build());

        assertThat(origin("GROUP", "18")).isEqualTo(ours);
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM \"group\" WHERE added_by = ?", Integer.class, Importer.ID.value()))
                .isZero();
        assertThat(jdbc.queryForObject(
                        "SELECT group_id FROM software_contribution WHERE software_id = ?",
                        UUID.class,
                        software("Elite")))
                .isEqualTo(ours);
    }

    @Test
    void takesForTheGroupOfTheArchiveAGroupNamedAsOneOfItsLabels() {
        UUID ours = ourGroup("Ebo Software", List.of("EBOSOFT"), null);

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(new SourceAuthor("18", new LabelName("EBOSOFT"), SourceAuthorKind.GROUP, null, null))
                .build());

        assertThat(origin("GROUP", "18")).isEqualTo(ours);
    }

    @Test
    void takesForTheGroupOfTheArchiveALabelOfASourceOfTheSameName() {
        UUID ours = ourGroup("EBOSOFT", List.of(), null);

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(SourceAuthor.label(new LabelName("EboSoft")))
                .build());

        assertThat(jdbc.queryForObject(
                        "SELECT group_id FROM software_contribution WHERE software_id = ?",
                        UUID.class,
                        software("Elite")))
                .isEqualTo(ours);
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM label WHERE added_by = ?", Integer.class, Importer.ID.value()))
                .isZero();
    }

    @Test
    void makesANewGroupOfOneWhoseNameOnlyBeginsAGroupOfTheArchive() {
        UUID ours = ourGroup("SOFT123", List.of(), null);

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(new SourceAuthor("18", new LabelName("SOFT"), SourceAuthorKind.GROUP, null, null))
                .build());

        assertThat(origin("GROUP", "18")).isNotEqualTo(ours);
    }

    @Test
    void makesANewGroupOfOneFromAnotherPlace() {
        UUID ours = ourGroup("EBOSOFT", List.of(), KNOWN_CITY);

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(new SourceAuthor(
                        "18",
                        new LabelName("EBOSOFT"),
                        SourceAuthorKind.GROUP,
                        null,
                        new SourcePlace("Новоград", Language.RU, SourcePlaceKind.CITY)))
                .build());

        assertThat(origin("GROUP", "18")).isNotEqualTo(ours);
    }

    @Test
    void makesANewGroupOfOneTwoGroupsOfTheArchiveMatch() {
        UUID one = ourGroup("EBOSOFT", List.of(), null);
        UUID other = ourGroup("Ebo Software", List.of("EBO-SOFT"), null);

        importProgram(program("1", "Elite", zip(Map.of("ELITE.ROM", ELITE_ROM)))
                .authors(new SourceAuthor("18", new LabelName("EBOSOFT"), SourceAuthorKind.GROUP, null, null))
                .build());

        assertThat(origin("GROUP", "18")).isNotIn(one, other);
    }

    private void importProgram(SourceProgram program) {
        ImportRunId run = new ImportRunId(UUID.randomUUID());
        runs.save(ImportRun.start(run, new ImportScope(ARCHIVE, null), Instant.now()));
        handler.handle(new ImportProgramCommand(run, program));
    }

    private SoftwareId ourSoftware(String title, byte[] file) {
        SoftwareId software = createSoftware.handle(new CreateSoftwareCommand(
                TestActors.ADMIN, new SoftwareTitle(title), null, null, VECTOR, Set.of(VECTOR_06C), Set.of()));
        createRelease.handle(new CreateReleaseCommand(
                TestActors.ADMIN,
                software,
                new ReleaseContent(null, null, ReleaseType.ORIGINAL, null, null),
                Set.of(),
                upload("ours.zip", file)));
        return software;
    }

    /** A person the archive has, added by an administrator before any import. */
    private UUID ourPerson(String realName, String nickname, @Nullable UUID city) {
        return createAuthor
                .handle(new CreateAuthorCommand(
                        TestActors.ADMIN,
                        new RealName(realName),
                        null,
                        List.of(new LabelDraft(null, new LabelName(nickname), true)),
                        locations(city)))
                .value();
    }

    /** A group the archive has, added by an administrator before any import. */
    private UUID ourGroup(String name, List<String> labels, @Nullable UUID city) {
        return createGroup
                .handle(new CreateGroupCommand(
                        TestActors.ADMIN,
                        new GroupName(name),
                        null,
                        Set.of(),
                        labels.stream()
                                .map(label -> new UnmarkedLabelDraft(null, new LabelName(label)))
                                .toList(),
                        locations(city)))
                .value();
    }

    private static List<LocationDraft> locations(@Nullable UUID city) {
        return city == null ? List.of() : List.of(new LocationDraft(null, new CityId(city), true));
    }

    private UUID labelOf(UUID person, String name) {
        return Objects.requireNonNull(
                jdbc.queryForObject("SELECT id FROM label WHERE person_id = ? AND name = ?", UUID.class, person, name));
    }

    private static SourceAuthor person(String id, String nickname, String realName, @Nullable SourcePlace place) {
        return new SourceAuthor(id, new LabelName(nickname), SourceAuthorKind.PERSON, new RealName(realName), place);
    }

    private ImportRunReport lastReport() {
        return reports.findLatest().orElseThrow();
    }

    private String preservation(UUID software) {
        return jdbc.queryForObject("SELECT preservation FROM software WHERE id = ?", String.class, software);
    }

    private UUID software(String title) {
        return jdbc.queryForObject("SELECT id FROM software WHERE title = ?", UUID.class, title);
    }

    private UUID origin(String kind, String externalId) {
        return jdbc.queryForObject(
                "SELECT entity_id FROM external_origin WHERE origin = ? AND kind = ? AND external_id = ?",
                UUID.class,
                ARCHIVE,
                kind,
                externalId);
    }

    private UUID cityOf(UUID entity) {
        return jdbc.queryForObject("SELECT city_id FROM entity_location WHERE entity_id = ?", UUID.class, entity);
    }

    private List<String> labelCredits(UUID software) {
        return jdbc.queryForList(
                "SELECT l.name FROM software_contribution c JOIN label l ON l.id = c.label_id"
                        + " WHERE c.software_id = ? ORDER BY l.name",
                String.class,
                software);
    }

    private List<String> screenshots(UUID software) {
        return jdbc.queryForList(
                "SELECT file_name || ':' || size FROM software_screenshot WHERE software_id = ?",
                String.class,
                software);
    }

    private static ProgramBuilder program(String id, String title, byte[] file) {
        return new ProgramBuilder(id, title, upload("program-" + id + ".zip", file));
    }

    private static ScreenshotImage screenshot(String name, int size) {
        byte[] picture = Arrays.copyOf(PNG, size);
        return ScreenshotImage.of(
                Arrays.copyOf(picture, ScreenshotImage.HEAD_BYTES),
                size,
                new ScreenshotFileName(name),
                () -> new ByteArrayInputStream(picture));
    }

    private static ReleaseArchiveUpload upload(String name, byte[] content) {
        return new ReleaseArchiveUpload(name, content.length, () -> new ByteArrayInputStream(content));
    }

    private static byte[] bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] zip(Map<String, byte[]> files) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, byte[]> file : new LinkedHashMap<>(files).entrySet()) {
                zip.putNextEntry(new ZipEntry(file.getKey()));
                zip.write(file.getValue());
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    /** A program of the test source; whatever a test does not set, the source does not tell. */
    private static final class ProgramBuilder {

        private final String id;
        private final String title;
        private final ReleaseArchiveUpload file;
        private @Nullable String description;
        private Set<HardwareCode> hardware = Set.of(VECTOR_06C);
        private Set<HardwareCode> releaseHardware = Set.of();
        private Set<CategoryId> categories = Set.of();
        private List<SourceAuthor> authors = List.of();
        private List<ScreenshotImage> screenshots = List.of();
        private PreservationStatus preservation = PreservationStatus.UNKNOWN;

        private ProgramBuilder(String id, String title, ReleaseArchiveUpload file) {
            this.id = id;
            this.title = title;
            this.file = file;
        }

        ProgramBuilder description(String value) {
            description = value;
            return this;
        }

        ProgramBuilder hardware(HardwareCode... codes) {
            hardware = Set.of(codes);
            return this;
        }

        ProgramBuilder releaseHardware(HardwareCode... codes) {
            releaseHardware = Set.of(codes);
            return this;
        }

        ProgramBuilder categories(CategoryId... ids) {
            categories = Set.of(ids);
            return this;
        }

        ProgramBuilder authors(SourceAuthor... values) {
            authors = List.of(values);
            return this;
        }

        ProgramBuilder screenshots(ScreenshotImage... values) {
            screenshots = List.of(values);
            return this;
        }

        ProgramBuilder preservation(PreservationStatus value) {
            preservation = value;
            return this;
        }

        SourceProgram build() {
            return new SourceProgram(
                    ARCHIVE,
                    id,
                    new ExternalPage("https://test-archive.invalid/" + id),
                    new SoftwareTitle(title),
                    description == null ? null : new SoftwareDescription(description),
                    ReleaseDate.ofYear(Year.of(1987)),
                    null,
                    preservation,
                    VECTOR,
                    hardware,
                    releaseHardware,
                    categories,
                    authors,
                    screenshots,
                    file);
        }
    }
}
