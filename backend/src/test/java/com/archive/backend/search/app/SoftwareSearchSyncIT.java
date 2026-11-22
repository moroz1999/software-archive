package com.archive.backend.search.app;

import static com.archive.backend.category.domain.CategoryTitleFixtures.titles;
import static com.archive.backend.geo.GeoTestRows.city;
import static com.archive.backend.geo.GeoTestRows.country;
import static com.archive.backend.user.TestActors.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.author.app.CreateAuthorCommand;
import com.archive.backend.author.app.CreateAuthorHandler;
import com.archive.backend.author.app.DeleteAuthorCommand;
import com.archive.backend.author.app.DeleteAuthorHandler;
import com.archive.backend.author.app.SupplementAuthorCommand;
import com.archive.backend.author.app.SupplementAuthorHandler;
import com.archive.backend.author.app.UpdateAuthorCommand;
import com.archive.backend.author.app.UpdateAuthorHandler;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.RealName;
import com.archive.backend.category.app.CreateCategoryCommand;
import com.archive.backend.category.app.CreateCategoryHandler;
import com.archive.backend.category.app.DeleteCategoryCommand;
import com.archive.backend.category.app.DeleteCategoryHandler;
import com.archive.backend.category.app.UpdateCategoryCommand;
import com.archive.backend.category.app.UpdateCategoryHandler;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.group.app.CreateGroupCommand;
import com.archive.backend.group.app.CreateGroupHandler;
import com.archive.backend.group.app.SupplementGroupCommand;
import com.archive.backend.group.app.SupplementGroupHandler;
import com.archive.backend.group.app.UpdateGroupCommand;
import com.archive.backend.group.app.UpdateGroupHandler;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.UnmarkedLabelDraft;
import com.archive.backend.search.TypesenseProbe;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.read.HardwareCount;
import com.archive.backend.search.read.SoftwareCatalogAnswer;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.software.app.AddScreenshotsCommand;
import com.archive.backend.software.app.AddScreenshotsHandler;
import com.archive.backend.software.app.CreateReleaseCommand;
import com.archive.backend.software.app.CreateReleaseHandler;
import com.archive.backend.software.app.CreateSoftwareCommand;
import com.archive.backend.software.app.CreateSoftwareHandler;
import com.archive.backend.software.app.DeleteScreenshotCommand;
import com.archive.backend.software.app.DeleteScreenshotHandler;
import com.archive.backend.software.app.DeleteSoftwareCommand;
import com.archive.backend.software.app.DeleteSoftwareHandler;
import com.archive.backend.software.app.UpdateSoftwareCommand;
import com.archive.backend.software.app.UpdateSoftwareHandler;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.ScreenshotImages;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.user.TestActors;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.typesense.TypesenseContainer;

/**
 * Changes of the archive reach the software documents of the search through the outbox and the worker
 * (docs/features/search.md#synchronization). Every change commits for real; the test drives the worker.
 */
@IntegrationTest
class SoftwareSearchSyncIT {

    // Of the tree the schema brings: Games → Action → Shooters.
    private static final UUID GAMES = UUID.fromString("081bf3a1-782b-5a8b-8d12-3b155e0a58cb");
    private static final UUID ACTION = UUID.fromString("93c89b73-d81b-5b8f-9318-6a52895305b3");
    private static final UUID SHOOTERS = UUID.fromString("36d8df17-2b5b-518b-ac38-c3df4913be05");
    private static final UUID DEMOSCENE = UUID.fromString("c35b7f9b-44a8-5570-9406-9e6dcb30b607");

    private static final UUID FINLAND = UUID.fromString("00000000-0000-0000-00ff-000000000001");
    private static final UUID LATVIA = UUID.fromString("00000000-0000-0000-00ff-000000000002");
    private static final UUID HELSINKI = UUID.fromString("00000000-0000-0000-00ff-000000000011");

    @Autowired
    private ProcessSearchOutboxHandler worker;

    @Autowired
    private CreateSoftwareHandler createSoftware;

    @Autowired
    private UpdateSoftwareHandler updateSoftware;

    @Autowired
    private DeleteSoftwareHandler deleteSoftware;

    @Autowired
    private CreateReleaseHandler createRelease;

    @Autowired
    private AddScreenshotsHandler addScreenshots;

    @Autowired
    private DeleteScreenshotHandler deleteScreenshot;

    @Autowired
    private CreateCategoryHandler createCategory;

    @Autowired
    private UpdateCategoryHandler updateCategory;

    @Autowired
    private DeleteCategoryHandler deleteCategory;

    @Autowired
    private CreateAuthorHandler createAuthor;

    @Autowired
    private UpdateAuthorHandler updateAuthor;

    @Autowired
    private DeleteAuthorHandler deleteAuthor;

    @Autowired
    private CreateGroupHandler createGroup;

    @Autowired
    private UpdateGroupHandler updateGroup;

    @Autowired
    private SupplementAuthorHandler supplementAuthor;

    @Autowired
    private SupplementGroupHandler supplementGroup;

    @Autowired
    private ListSoftwareCatalogHandler catalog;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TypesenseContainer typesense;

    private TypesenseProbe probe;

    private final List<UUID> software = new ArrayList<>();

    @BeforeEach
    void setUp() {
        probe = new TypesenseProbe(typesense);
        TestActors.store(jdbc);
        country(jdbc, FINLAND, "Финляндия", "Finland");
        country(jdbc, LATVIA, "Латвия", "Latvia");
        city(jdbc, HELSINKI, FINLAND, "Хельсинки", "Helsinki");
    }

    /** Puts back what the tests committed. */
    @AfterEach
    void putBack() {
        probe.resume();
        TestActors.remove(jdbc);
        software.forEach(id -> jdbc.update("DELETE FROM software WHERE id = ?", id));
        jdbc.update("UPDATE category SET parent_id = ? WHERE id = ?", GAMES, ACTION);
        jdbc.update("DELETE FROM entity_location WHERE country_id IN (?, ?)", FINLAND, LATVIA);
        jdbc.update("DELETE FROM label WHERE name IN ('Zed', 'ZS', 'ZS Two')");
        jdbc.update("DELETE FROM person WHERE real_name LIKE 'Zed Zedson%'");
        jdbc.update(
                "DELETE FROM group_group_type WHERE group_id IN (SELECT id FROM \"group\" WHERE name = 'Zed Squad')");
        jdbc.update("DELETE FROM \"group\" WHERE name = 'Zed Squad'");
        jdbc.update("DELETE FROM city WHERE id = ?", HELSINKI);
        jdbc.update("DELETE FROM country WHERE id IN (?, ?)", FINLAND, LATVIA);
        jdbc.update("DELETE FROM search_outbox");
    }

    private SoftwareId create(String title, @Nullable ReleaseDate releaseDate, Set<UUID> categories) {
        SoftwareId id = createSoftware.handle(new CreateSoftwareCommand(
                ADMIN,
                new SoftwareTitle(title),
                null,
                releaseDate,
                new PlatformCode("vector"),
                Set.of(),
                categories.stream().map(CategoryId::new).collect(java.util.stream.Collectors.toSet())));
        software.add(id.value());
        return id;
    }

    private SoftwareId create(String title) {
        return create(title, ReleaseDate.ofDay(LocalDate.of(1987, 5, 1)), Set.of());
    }

    private Map<String, Object> document(SoftwareId id) throws Exception {
        return probe.software(id.value()).orElseThrow(() -> new AssertionError("no document of " + id));
    }

    /** A list field of a document; an absent one is empty. */
    private static List<Object> listIn(Map<String, Object> doc, String field) {
        return doc.get(field) instanceof List<?> list ? List.copyOf(list) : List.of();
    }

    private void credit(SoftwareId software, LabelId label) {
        jdbc.update(
                "INSERT INTO software_contribution (software_id, label_id) VALUES (?, ?)",
                software.value(),
                label.value());
    }

    private LabelId labelNamed(String name) {
        return new LabelId(jdbc.queryForObject("SELECT id FROM label WHERE name = ?", UUID.class, name));
    }

    private static LocationDraft in(UUID country) {
        return new LocationDraft(new CountryId(country), null, true);
    }

    private static LabelDraft zed(@Nullable LabelId id) {
        return new LabelDraft(id, new LabelName("Zed"), true);
    }

    @Test
    void indexesCreatedSoftwareWithItsTitlesPlatformYearAndCategoryBranch() throws Exception {
        SoftwareId elite = create("Elite", ReleaseDate.ofDay(LocalDate.of(1987, 5, 1)), Set.of(SHOOTERS));
        jdbc.update("INSERT INTO software_alternative_titles (software_id, title) VALUES (?, 'Элита')", elite.value());

        worker.handle();

        Map<String, Object> doc = document(elite);
        assertThat(doc)
                .containsEntry("title", "Elite")
                .containsEntry("platform", "vector")
                .containsEntry("release_date", "1987-05-01")
                .containsEntry("year", 1987);
        assertThat(listIn(doc, "alternative_titles")).containsExactly("Элита");
        assertThat(listIn(doc, "categories"))
                .containsExactlyInAnyOrder(SHOOTERS.toString(), ACTION.toString(), GAMES.toString());
    }

    @Test
    void indexesSoftwareOfWhichOnlyTheYearIsKnownUnderThatYear() throws Exception {
        SoftwareId joeBlade = create("Joe Blade", ReleaseDate.ofYear(Year.of(1987)), Set.of());

        worker.handle();

        assertThat(document(joeBlade)).containsEntry("release_date", "1987").containsEntry("year", 1987);
    }

    @Test
    void indexesSoftwareWithAnUnknownReleaseDateWithoutAYear() throws Exception {
        SoftwareId elite = create("Elite", null, Set.of());

        worker.handle();

        assertThat(document(elite)).doesNotContainKey("year");
    }

    private List<ScreenshotId> screenshots(SoftwareId software, @Nullable ReleaseId release, int count) {
        List<ScreenshotImage> images = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            images.add(ScreenshotImages.image(ScreenshotImages.png()));
        }
        return addScreenshots.handle(new AddScreenshotsCommand(ADMIN, software, release, images));
    }

    private static List<String> strings(List<ScreenshotId> ids) {
        return ids.stream().map(id -> id.value().toString()).toList();
    }

    @Test
    void indexesTheScreenshotsTheCardShowsOwnFirstThoseOfTheReleasesWithoutThem() throws Exception {
        SoftwareId elite = create("Elite");
        ReleaseId ayMod = createRelease.handle(new CreateReleaseCommand(
                ADMIN, elite, new ReleaseContent(null, null, ReleaseType.MOD, null, null), Set.of()));
        List<ScreenshotId> ofRelease = screenshots(elite, ayMod, 2);
        worker.handle();
        assertThat(listIn(document(elite), "screenshots")).containsExactlyElementsOf(strings(ofRelease));

        List<ScreenshotId> own = screenshots(elite, null, 1);
        worker.handle();
        assertThat(listIn(document(elite), "screenshots")).containsExactlyElementsOf(strings(own));

        deleteScreenshot.handle(new DeleteScreenshotCommand(ADMIN, elite, null, own.get(0)));
        worker.handle();
        assertThat(listIn(document(elite), "screenshots")).containsExactlyElementsOf(strings(ofRelease));
    }

    @Test
    void reindexesEditedSoftware() throws Exception {
        SoftwareId elite = create("Elite");
        worker.handle();

        updateSoftware.handle(new UpdateSoftwareCommand(
                ADMIN,
                elite,
                new SoftwareTitle("Elite Plus"),
                null,
                null,
                new PlatformCode("vector"),
                Set.of(),
                Set.of()));
        worker.handle();

        assertThat(document(elite)).containsEntry("title", "Elite Plus");
    }

    @Test
    void removesDeletedSoftwareFromTheIndex() throws Exception {
        SoftwareId elite = create("Elite");
        worker.handle();
        assertThat(probe.software(elite.value())).isPresent();

        deleteSoftware.handle(new DeleteSoftwareCommand(ADMIN, elite));
        worker.handle();

        assertThat(probe.software(elite.value())).isEmpty();
    }

    @Test
    void takesSoftwareDeletedBeforeItWasIndexedAsDone() throws Exception {
        SoftwareId elite = create("Elite");
        deleteSoftware.handle(new DeleteSoftwareCommand(ADMIN, elite));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM search_outbox", Integer.class))
                .isOne();

        worker.handle();

        assertThat(probe.software(elite.value())).isEmpty();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM search_outbox", Integer.class))
                .isZero();
    }

    @Test
    void movingACategoryChangesTheBranchOfTheSoftwareBelowIt() throws Exception {
        SoftwareId elite = create("Elite", ReleaseDate.ofDay(LocalDate.of(1987, 5, 1)), Set.of(SHOOTERS));
        worker.handle();

        updateCategory.handle(
                new UpdateCategoryCommand(ADMIN, new CategoryId(ACTION), titles("Action"), new CategoryId(DEMOSCENE)));
        worker.handle();

        assertThat(listIn(document(elite), "categories"))
                .containsExactlyInAnyOrder(SHOOTERS.toString(), ACTION.toString(), DEMOSCENE.toString());
    }

    @Test
    void deletingACategoryTakesItOutOfTheSoftwareInItsSubtree() throws Exception {
        CategoryId space =
                createCategory.handle(new CreateCategoryCommand(ADMIN, titles("Space"), new CategoryId(GAMES)));
        CategoryId trading = createCategory.handle(new CreateCategoryCommand(ADMIN, titles("Trading"), space));
        SoftwareId elite = create("Elite", ReleaseDate.ofDay(LocalDate.of(1987, 5, 1)), Set.of(trading.value()));
        worker.handle();

        deleteCategory.handle(new DeleteCategoryCommand(ADMIN, space));
        worker.handle();

        assertThat(listIn(document(elite), "categories")).isEmpty();
    }

    @Test
    void theCountriesOfSoftwareAreWhereTheOwnersOfItsCreditedLabelsAre() throws Exception {
        createAuthor.handle(new CreateAuthorCommand(
                ADMIN, new RealName("Zed Zedson"), null, List.of(zed(null)), List.of(in(FINLAND))));
        SoftwareId elite = create("Elite");
        credit(elite, labelNamed("Zed"));

        worker.handle();

        assertThat(listIn(document(elite), "countries")).containsExactly(FINLAND.toString());
    }

    @Test
    void theCitiesOfSoftwareAreWhereTheOwnersOfItsCreditedLabelsAre() throws Exception {
        createAuthor.handle(new CreateAuthorCommand(
                ADMIN,
                new RealName("Zed Zedson"),
                null,
                List.of(zed(null)),
                List.of(new LocationDraft(new CountryId(FINLAND), new CityId(HELSINKI), true))));
        SoftwareId elite = create("Elite");
        credit(elite, labelNamed("Zed"));

        worker.handle();

        assertThat(listIn(document(elite), "cities")).containsExactly(HELSINKI.toString());
        assertThat(listIn(document(elite), "countries")).containsExactly(FINLAND.toString());
    }

    private SoftwareId createWithHardware(String title, String... hardware) {
        SoftwareId id = createSoftware.handle(new CreateSoftwareCommand(
                ADMIN, new SoftwareTitle(title), null, null, new PlatformCode("vector"), codes(hardware), Set.of()));
        software.add(id.value());
        return id;
    }

    private static Set<HardwareCode> codes(String... hardware) {
        return java.util.Arrays.stream(hardware).map(HardwareCode::new).collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void indexesTheHardwareOfSoftwareAndOfEveryReleaseOfItByGroup() throws Exception {
        SoftwareId elite = createWithHardware("Elite", "vector06c", "tape");
        worker.handle();
        assertThat(listIn(document(elite), "hardware_computers")).containsExactly("vector06c");

        createRelease.handle(new CreateReleaseCommand(
                ADMIN,
                elite,
                new ReleaseContent(null, null, ReleaseType.MOD, null, null),
                codes("krista2", "ay8910"),
                null));
        worker.handle();

        Map<String, Object> doc = document(elite);
        assertThat(listIn(doc, "hardware_computers")).containsExactlyInAnyOrder("vector06c", "krista2");
        assertThat(listIn(doc, "hardware_storage")).containsExactly("tape");
        assertThat(listIn(doc, "hardware_sound")).containsExactly("ay8910");
    }

    private SoftwareCatalogAnswer withHardware(String keyword, Language language, String... hardware) {
        return catalog.handle(new ListSoftwareCatalogQuery(
                new CatalogKeyword(keyword),
                new PlatformCode("vector"),
                null,
                null,
                null,
                null,
                List.of(),
                java.util.Arrays.stream(hardware).map(HardwareCode::new).toList(),
                language,
                PageRequest.of(0, 20)));
    }

    private List<String> foundWithHardware(String keyword, String... hardware) {
        return withHardware(keyword, Language.EN, hardware).catalog().items().getContent().stream()
                .map(SoftwareCatalogItem::title)
                .toList();
    }

    @Test
    void theCatalogTellsTheGroupsOfTheHardwareChosenByTheReferenceBook() throws Exception {
        createWithHardware("Kappa One", "vector06c", "ay8910");
        createWithHardware("Kappa Two", "krista2");
        worker.handle();

        assertThat(foundWithHardware("Kappa", "vector06c", "krista2")).containsExactly("Kappa One", "Kappa Two");
        assertThat(foundWithHardware("Kappa", "vector06c", "krista2", "ay8910")).containsExactly("Kappa One");
    }

    @Test
    void offersTheHardwareOfTheSoftwareFoundNamedByTheReferenceBookAndKeepsTheChosenWithoutAny() throws Exception {
        createWithHardware("Lambda One", "vector06c", "ay8910");
        createWithHardware("Lambda Two", "vector06c");
        createWithHardware("Sigma", "vector06c");
        worker.handle();

        assertThat(withHardware("Lambda", Language.RU).hardware())
                .containsExactly(
                        new HardwareCount("vector06c", "Вектор-06Ц", "computers", 2),
                        new HardwareCount("ay8910", "AY-3-8910", "sound", 1));
        assertThat(withHardware("Sigma", Language.EN, "ay8910").hardware())
                .containsExactly(new HardwareCount("ay8910", "AY-3-8910", "sound", 0));
    }

    @Test
    void hardwareTheArchiveDoesNotHaveFindsNothing() throws Exception {
        createWithHardware("Omicron", "vector06c");
        worker.handle();

        assertThat(foundWithHardware("Omicron", "vector06c")).containsExactly("Omicron");
        assertThat(foundWithHardware("Omicron", "vector06c", "zx-interface")).isEmpty();
    }

    @Test
    void theCountriesOfSoftwareLeaveOutWhoOnlyCrackedAdaptedTranslatedOrHelpedWithIt() throws Exception {
        createAuthor.handle(new CreateAuthorCommand(
                ADMIN, new RealName("Zed Zedson"), null, List.of(zed(null)), List.of(in(FINLAND))));
        SoftwareId elite = create("Elite");
        jdbc.update("""
                INSERT INTO software_contribution (software_id, label_id, role_codes) VALUES (?, ?, ARRAY['release',
                    'intro_code', 'intro_graphics', 'intro_music', 'adaptation', 'restoring', 'localization',
                    'translation', 'guest', 'support', 'testing'])
                """, elite.value(), labelNamed("Zed").value());

        worker.handle();

        assertThat(listIn(document(elite), "countries")).isEmpty();
    }

    @Test
    void movingAnAuthorChangesTheCountriesOfTheirSoftware() throws Exception {
        AuthorId author = createAuthor.handle(new CreateAuthorCommand(
                ADMIN, new RealName("Zed Zedson"), null, List.of(zed(null)), List.of(in(FINLAND))));
        LabelId label = labelNamed("Zed");
        SoftwareId elite = create("Elite");
        credit(elite, label);
        worker.handle();

        updateAuthor.handle(new UpdateAuthorCommand(
                ADMIN, author, new RealName("Zed Zedson"), null, List.of(zed(label)), List.of(in(LATVIA))));
        worker.handle();

        assertThat(listIn(document(elite), "countries")).containsExactly(LATVIA.toString());
    }

    @Test
    void placesAnImportGivesAPersonReachTheSoftwareCreditedUnderTheirLabels() throws Exception {
        AuthorId author = createAuthor.handle(
                new CreateAuthorCommand(ADMIN, new RealName("Zed Zedson"), null, List.of(zed(null)), List.of()));
        SoftwareId elite = create("Elite");
        credit(elite, labelNamed("Zed"));
        worker.handle();

        supplementAuthor.handle(new SupplementAuthorCommand(
                ADMIN, author, null, List.of(new LocationDraft(new CountryId(FINLAND), new CityId(HELSINKI), true))));
        worker.handle();

        assertThat(listIn(document(elite), "countries")).containsExactly(FINLAND.toString());
        assertThat(listIn(document(elite), "cities")).containsExactly(HELSINKI.toString());
    }

    @Test
    void placesAnImportGivesAGroupReachTheSoftwareCreditedUnderItsLabels() throws Exception {
        GroupId group = zedSquad();
        SoftwareId elite = create("Elite");
        credit(elite, labelNamed("ZS"));
        worker.handle();

        supplementGroup.handle(new SupplementGroupCommand(
                ADMIN, group, List.of(new LocationDraft(new CountryId(FINLAND), new CityId(HELSINKI), true))));
        worker.handle();

        assertThat(listIn(document(elite), "countries")).containsExactly(FINLAND.toString());
        assertThat(listIn(document(elite), "cities")).containsExactly(HELSINKI.toString());
    }

    @Test
    void aLabelTakenFromItsOwnerTakesTheOwnersCountriesFromItsSoftware() throws Exception {
        AuthorId author = createAuthor.handle(new CreateAuthorCommand(
                ADMIN, new RealName("Zed Zedson"), null, List.of(zed(null)), List.of(in(FINLAND))));
        SoftwareId elite = create("Elite");
        credit(elite, labelNamed("Zed"));
        worker.handle();

        updateAuthor.handle(new UpdateAuthorCommand(
                ADMIN, author, new RealName("Zed Zedson"), null, List.of(), List.of(in(FINLAND))));
        worker.handle();

        assertThat(listIn(document(elite), "countries")).isEmpty();
    }

    @Test
    void deletingAnAuthorTakesTheirCountriesFromTheirSoftware() throws Exception {
        AuthorId author = createAuthor.handle(new CreateAuthorCommand(
                ADMIN, new RealName("Zed Zedson"), null, List.of(zed(null)), List.of(in(FINLAND))));
        SoftwareId elite = create("Elite");
        credit(elite, labelNamed("Zed"));
        worker.handle();

        deleteAuthor.handle(new DeleteAuthorCommand(ADMIN, author));
        worker.handle();

        assertThat(listIn(document(elite), "countries")).isEmpty();
    }

    private GroupId zedSquad() {
        return createGroup.handle(new CreateGroupCommand(
                ADMIN,
                new GroupName("Zed Squad"),
                null,
                Set.of(new GroupTypeCode("demogroup")),
                List.of(new UnmarkedLabelDraft(null, new LabelName("ZS"))),
                List.of()));
    }

    /** The labels of the group as they are, for an update that changes something else. */
    private List<UnmarkedLabelDraft> keptLabels(GroupId group) {
        return jdbc.query(
                "SELECT id, name FROM label WHERE group_id = ? ORDER BY name",
                (rs, row) -> new UnmarkedLabelDraft(
                        new LabelId(rs.getObject("id", UUID.class)), new LabelName(rs.getString("name"))),
                group.value());
    }

    private void updateZedSquad(GroupId group, List<UnmarkedLabelDraft> labels) {
        updateZedSquad(group, new GroupName("Zed Squad"), labels);
    }

    private void updateZedSquad(GroupId group, GroupName name, List<UnmarkedLabelDraft> labels) {
        updateGroup.handle(new UpdateGroupCommand(
                ADMIN, group, name, null, Set.of(new GroupTypeCode("demogroup")), labels, List.of()));
    }

    @Test
    void indexesTheNamesOfWhoMadeTheSoftwareForItsCard() throws Exception {
        createAuthor.handle(
                new CreateAuthorCommand(ADMIN, new RealName("Zed Zedson"), null, List.of(zed(null)), List.of()));
        SoftwareId elite = create("Elite");
        credit(elite, labelNamed("Zed"));

        worker.handle();

        assertThat(listIn(document(elite), "made_by")).containsExactly("Zed");
    }

    @Test
    void renamingADeveloperRenamesItOnTheCardsOfItsSoftware() throws Exception {
        GroupId group = zedSquad();
        SoftwareId elite = create("Elite");
        jdbc.update(
                "INSERT INTO software_contribution (software_id, group_id, role_codes) VALUES (?, ?, ARRAY['developer'])",
                elite.value(),
                group.value());
        worker.handle();
        assertThat(listIn(document(elite), "made_by")).containsExactly("Zed Squad");

        updateZedSquad(group, new GroupName("Zed Crew"), keptLabels(group));
        worker.handle();

        assertThat(listIn(document(elite), "made_by")).containsExactly("Zed Crew");
    }

    @Test
    void renamingTheLabelADeveloperMadeSoftwareUnderRenamesItOnTheCard() throws Exception {
        GroupId group = zedSquad();
        LabelId label = labelNamed("ZS");
        SoftwareId elite = create("Elite");
        jdbc.update(
                "INSERT INTO software_contribution (software_id, label_id, role_codes) VALUES (?, ?, ARRAY['developer'])",
                elite.value(),
                label.value());
        worker.handle();

        updateZedSquad(group, List.of(new UnmarkedLabelDraft(label, new LabelName("ZS Two"))));
        worker.handle();

        assertThat(listIn(document(elite), "made_by")).containsExactly("ZS Two");
    }

    @Test
    void renamingACreditedPersonRenamesThemOnTheCardsOfTheirSoftware() throws Exception {
        AuthorId author = createAuthor.handle(
                new CreateAuthorCommand(ADMIN, new RealName("Zed Zedson"), null, List.of(), List.of()));
        SoftwareId elite = create("Elite");
        jdbc.update(
                "INSERT INTO software_contribution (software_id, person_id) VALUES (?, ?)",
                elite.value(),
                author.value());
        worker.handle();
        assertThat(listIn(document(elite), "made_by")).containsExactly("Zed Zedson");

        updateAuthor.handle(
                new UpdateAuthorCommand(ADMIN, author, new RealName("Zed Zedson Two"), null, List.of(), List.of()));
        worker.handle();

        assertThat(listIn(document(elite), "made_by")).containsExactly("Zed Zedson Two");
    }

    @Test
    void movingAGroupCreditedByItselfChangesTheCountriesOfItsSoftware() throws Exception {
        GroupId group = createGroup.handle(new CreateGroupCommand(
                ADMIN,
                new GroupName("Zed Squad"),
                null,
                Set.of(new GroupTypeCode("demogroup")),
                List.of(new UnmarkedLabelDraft(null, new LabelName("ZS"))),
                List.of(in(FINLAND))));
        LabelId label = labelNamed("ZS");
        SoftwareId elite = create("Elite");
        jdbc.update(
                "INSERT INTO software_contribution (software_id, group_id, role_codes) VALUES (?, ?, ARRAY['developer'])",
                elite.value(),
                group.value());
        worker.handle();
        assertThat(listIn(document(elite), "countries")).containsExactly(FINLAND.toString());

        updateGroup.handle(new UpdateGroupCommand(
                ADMIN,
                group,
                new GroupName("Zed Squad"),
                null,
                Set.of(new GroupTypeCode("demogroup")),
                List.of(new UnmarkedLabelDraft(label, new LabelName("ZS"))),
                List.of(in(LATVIA))));
        worker.handle();

        assertThat(listIn(document(elite), "countries")).containsExactly(LATVIA.toString());
    }

    @Test
    void movingAGroupChangesTheCountriesOfItsSoftware() throws Exception {
        GroupId group = createGroup.handle(new CreateGroupCommand(
                ADMIN,
                new GroupName("Zed Squad"),
                null,
                Set.of(new GroupTypeCode("demogroup")),
                List.of(new UnmarkedLabelDraft(null, new LabelName("ZS"))),
                List.of(in(FINLAND))));
        LabelId label = labelNamed("ZS");
        SoftwareId elite = create("Elite");
        credit(elite, label);
        worker.handle();

        updateGroup.handle(new UpdateGroupCommand(
                ADMIN,
                group,
                new GroupName("Zed Squad"),
                null,
                Set.of(new GroupTypeCode("demogroup")),
                List.of(new UnmarkedLabelDraft(label, new LabelName("ZS"))),
                List.of(in(LATVIA))));
        worker.handle();

        assertThat(listIn(document(elite), "countries")).containsExactly(LATVIA.toString());
    }

    @Test
    void anEditIsSavedWhileTypesenseIsDownAndReachesTheIndexOnceItIsBack() throws Exception {
        SoftwareId elite = create("Elite");
        worker.handle();

        probe.freeze();
        updateSoftware.handle(new UpdateSoftwareCommand(
                ADMIN,
                elite,
                new SoftwareTitle("Elite Plus"),
                null,
                null,
                new PlatformCode("vector"),
                Set.of(),
                Set.of()));
        worker.handle();
        probe.resume();

        assertThat(jdbc.queryForObject("SELECT title FROM software WHERE id = ?", String.class, elite.value()))
                .isEqualTo("Elite Plus");
        // A call that timed out may still reach a frozen Typesense once it is back, so only the task tells.
        assertThat(jdbc.queryForObject("SELECT count(*) FROM search_outbox", Integer.class))
                .isOne();

        // The retry delay has passed.
        jdbc.update("UPDATE search_outbox SET next_attempt_at = now()");
        worker.handle();

        assertThat(document(elite)).containsEntry("title", "Elite Plus");
    }

    @Test
    void aDeletionWhileTypesenseIsDownReachesTheIndexOnceItIsBack() throws Exception {
        SoftwareId elite = create("Elite");
        worker.handle();
        assertThat(probe.software(elite.value())).isPresent();

        probe.freeze();
        deleteSoftware.handle(new DeleteSoftwareCommand(ADMIN, elite));
        worker.handle();
        probe.resume();
        jdbc.update("UPDATE search_outbox SET next_attempt_at = now()");
        worker.handle();

        assertThat(probe.software(elite.value())).isEmpty();
    }
}
