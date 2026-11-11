package com.archive.backend.search.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.archive.backend.author.app.CreateAuthorCommand;
import com.archive.backend.author.app.CreateAuthorHandler;
import com.archive.backend.author.app.DeleteAuthorCommand;
import com.archive.backend.author.app.DeleteAuthorHandler;
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
import com.archive.backend.category.domain.CategoryTitle;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.group.app.CreateGroupCommand;
import com.archive.backend.group.app.CreateGroupHandler;
import com.archive.backend.group.app.UpdateGroupCommand;
import com.archive.backend.group.app.UpdateGroupHandler;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.search.TypesenseProbe;
import com.archive.backend.software.app.CreateSoftwareCommand;
import com.archive.backend.software.app.CreateSoftwareHandler;
import com.archive.backend.software.app.DeleteSoftwareCommand;
import com.archive.backend.software.app.DeleteSoftwareHandler;
import com.archive.backend.software.app.UpdateSoftwareCommand;
import com.archive.backend.software.app.UpdateSoftwareHandler;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import java.time.LocalDate;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.typesense.TypesenseContainer;

/**
 * Changes of the archive reach the software documents of the search through the outbox and the worker
 * (docs/features/search.md#synchronization). Every change commits for real; the test drives the worker.
 */
@SpringBootTest
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class})
class SoftwareSearchSyncIT {

    // Of the tree the schema brings: Games → Action → Shooters.
    private static final UUID GAMES = UUID.fromString("081bf3a1-782b-5a8b-8d12-3b155e0a58cb");
    private static final UUID ACTION = UUID.fromString("93c89b73-d81b-5b8f-9318-6a52895305b3");
    private static final UUID SHOOTERS = UUID.fromString("36d8df17-2b5b-518b-ac38-c3df4913be05");
    private static final UUID DEMOSCENE = UUID.fromString("c35b7f9b-44a8-5570-9406-9e6dcb30b607");

    private static final UUID FINLAND = UUID.fromString("00000000-0000-0000-00ff-000000000001");
    private static final UUID LATVIA = UUID.fromString("00000000-0000-0000-00ff-000000000002");

    @Autowired
    private ProcessSearchOutboxHandler worker;

    @Autowired
    private CreateSoftwareHandler createSoftware;

    @Autowired
    private UpdateSoftwareHandler updateSoftware;

    @Autowired
    private DeleteSoftwareHandler deleteSoftware;

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
    private JdbcTemplate jdbc;

    @Autowired
    private TypesenseContainer typesense;

    private TypesenseProbe probe;

    private final List<UUID> software = new ArrayList<>();

    @BeforeEach
    void setUp() {
        probe = new TypesenseProbe(typesense);
        jdbc.update("INSERT INTO country (id, name) VALUES (?, 'Finland'), (?, 'Latvia')", FINLAND, LATVIA);
    }

    /** Puts back what the tests committed. */
    @AfterEach
    void putBack() {
        probe.resume();
        software.forEach(id -> jdbc.update("DELETE FROM software WHERE id = ?", id));
        jdbc.update("UPDATE category SET parent_id = ? WHERE id = ?", GAMES, ACTION);
        jdbc.update("DELETE FROM entity_location WHERE country_id IN (?, ?)", FINLAND, LATVIA);
        jdbc.update("DELETE FROM label WHERE name IN ('Zed', 'ZS')");
        jdbc.update("DELETE FROM person WHERE real_name = 'Zed Zedson'");
        jdbc.update(
                "DELETE FROM group_group_type WHERE group_id IN (SELECT id FROM \"group\" WHERE name = 'Zed Squad')");
        jdbc.update("DELETE FROM \"group\" WHERE name = 'Zed Squad'");
        jdbc.update("DELETE FROM country WHERE id IN (?, ?)", FINLAND, LATVIA);
        jdbc.update("DELETE FROM search_outbox");
    }

    private SoftwareId create(String title, @Nullable LocalDate releaseDate, Set<UUID> categories) {
        SoftwareId id = createSoftware.handle(new CreateSoftwareCommand(
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
        return create(title, LocalDate.of(1987, 5, 1), Set.of());
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
        SoftwareId elite = create("Elite", LocalDate.of(1987, 5, 1), Set.of(SHOOTERS));
        jdbc.update("INSERT INTO software_alternative_titles (software_id, title) VALUES (?, 'Элита')", elite.value());

        worker.handle();

        Map<String, Object> doc = document(elite);
        assertThat(doc)
                .containsEntry("title", "Elite")
                .containsEntry("platform", "vector")
                .containsEntry("year", 1987);
        assertThat(listIn(doc, "alternative_titles")).containsExactly("Элита");
        assertThat(listIn(doc, "categories"))
                .containsExactlyInAnyOrder(SHOOTERS.toString(), ACTION.toString(), GAMES.toString());
    }

    @Test
    void indexesSoftwareWithAnUnknownReleaseDateWithoutAYear() throws Exception {
        SoftwareId elite = create("Elite", null, Set.of());

        worker.handle();

        assertThat(document(elite)).doesNotContainKey("year");
    }

    @Test
    void reindexesEditedSoftware() throws Exception {
        SoftwareId elite = create("Elite");
        worker.handle();

        updateSoftware.handle(new UpdateSoftwareCommand(
                elite, new SoftwareTitle("Elite Plus"), null, null, new PlatformCode("vector"), Set.of(), Set.of()));
        worker.handle();

        assertThat(document(elite)).containsEntry("title", "Elite Plus");
    }

    @Test
    void removesDeletedSoftwareFromTheIndex() throws Exception {
        SoftwareId elite = create("Elite");
        worker.handle();
        assertThat(probe.software(elite.value())).isPresent();

        deleteSoftware.handle(new DeleteSoftwareCommand(elite));
        worker.handle();

        assertThat(probe.software(elite.value())).isEmpty();
    }

    @Test
    void takesSoftwareDeletedBeforeItWasIndexedAsDone() throws Exception {
        SoftwareId elite = create("Elite");
        deleteSoftware.handle(new DeleteSoftwareCommand(elite));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM search_outbox", Integer.class))
                .isOne();

        worker.handle();

        assertThat(probe.software(elite.value())).isEmpty();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM search_outbox", Integer.class))
                .isZero();
    }

    @Test
    void movingACategoryChangesTheBranchOfTheSoftwareBelowIt() throws Exception {
        SoftwareId elite = create("Elite", LocalDate.of(1987, 5, 1), Set.of(SHOOTERS));
        worker.handle();

        updateCategory.handle(new UpdateCategoryCommand(
                new CategoryId(ACTION), new CategoryTitle("Action"), new CategoryId(DEMOSCENE)));
        worker.handle();

        assertThat(listIn(document(elite), "categories"))
                .containsExactlyInAnyOrder(SHOOTERS.toString(), ACTION.toString(), DEMOSCENE.toString());
    }

    @Test
    void deletingACategoryTakesItOutOfTheSoftwareInItsSubtree() throws Exception {
        CategoryId space =
                createCategory.handle(new CreateCategoryCommand(new CategoryTitle("Space"), new CategoryId(GAMES)));
        CategoryId trading = createCategory.handle(new CreateCategoryCommand(new CategoryTitle("Trading"), space));
        SoftwareId elite = create("Elite", LocalDate.of(1987, 5, 1), Set.of(trading.value()));
        worker.handle();

        deleteCategory.handle(new DeleteCategoryCommand(space));
        worker.handle();

        assertThat(listIn(document(elite), "categories")).isEmpty();
    }

    @Test
    void theCountriesOfSoftwareAreWhereTheOwnersOfItsCreditedLabelsAre() throws Exception {
        createAuthor.handle(
                new CreateAuthorCommand(new RealName("Zed Zedson"), null, List.of(zed(null)), List.of(in(FINLAND))));
        SoftwareId elite = create("Elite");
        credit(elite, labelNamed("Zed"));

        worker.handle();

        assertThat(listIn(document(elite), "countries")).containsExactly(FINLAND.toString());
    }

    @Test
    void movingAnAuthorChangesTheCountriesOfTheirSoftware() throws Exception {
        AuthorId author = createAuthor.handle(
                new CreateAuthorCommand(new RealName("Zed Zedson"), null, List.of(zed(null)), List.of(in(FINLAND))));
        LabelId label = labelNamed("Zed");
        SoftwareId elite = create("Elite");
        credit(elite, label);
        worker.handle();

        updateAuthor.handle(new UpdateAuthorCommand(
                author, new RealName("Zed Zedson"), null, List.of(zed(label)), List.of(in(LATVIA))));
        worker.handle();

        assertThat(listIn(document(elite), "countries")).containsExactly(LATVIA.toString());
    }

    @Test
    void aLabelTakenFromItsOwnerTakesTheOwnersCountriesFromItsSoftware() throws Exception {
        AuthorId author = createAuthor.handle(
                new CreateAuthorCommand(new RealName("Zed Zedson"), null, List.of(zed(null)), List.of(in(FINLAND))));
        SoftwareId elite = create("Elite");
        credit(elite, labelNamed("Zed"));
        worker.handle();

        updateAuthor.handle(
                new UpdateAuthorCommand(author, new RealName("Zed Zedson"), null, List.of(), List.of(in(FINLAND))));
        worker.handle();

        assertThat(listIn(document(elite), "countries")).isEmpty();
    }

    @Test
    void deletingAnAuthorTakesTheirCountriesFromTheirSoftware() throws Exception {
        AuthorId author = createAuthor.handle(
                new CreateAuthorCommand(new RealName("Zed Zedson"), null, List.of(zed(null)), List.of(in(FINLAND))));
        SoftwareId elite = create("Elite");
        credit(elite, labelNamed("Zed"));
        worker.handle();

        deleteAuthor.handle(new DeleteAuthorCommand(author));
        worker.handle();

        assertThat(listIn(document(elite), "countries")).isEmpty();
    }

    @Test
    void movingAGroupChangesTheCountriesOfItsSoftware() throws Exception {
        GroupId group = createGroup.handle(new CreateGroupCommand(
                new GroupName("Zed Squad"),
                null,
                Set.of(new GroupTypeCode("demogroup")),
                List.of(new LabelDraft(null, new LabelName("ZS"), true)),
                List.of(in(FINLAND))));
        LabelId label = labelNamed("ZS");
        SoftwareId elite = create("Elite");
        credit(elite, label);
        worker.handle();

        updateGroup.handle(new UpdateGroupCommand(
                group,
                new GroupName("Zed Squad"),
                null,
                Set.of(new GroupTypeCode("demogroup")),
                List.of(new LabelDraft(label, new LabelName("ZS"), true)),
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
                elite, new SoftwareTitle("Elite Plus"), null, null, new PlatformCode("vector"), Set.of(), Set.of()));
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
        deleteSoftware.handle(new DeleteSoftwareCommand(elite));
        worker.handle();
        probe.resume();
        jdbc.update("UPDATE search_outbox SET next_attempt_at = now()");
        worker.handle();

        assertThat(probe.software(elite.value())).isEmpty();
    }
}
