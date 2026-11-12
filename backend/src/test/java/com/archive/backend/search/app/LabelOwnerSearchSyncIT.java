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
import com.archive.backend.group.app.CreateGroupCommand;
import com.archive.backend.group.app.CreateGroupHandler;
import com.archive.backend.group.app.DeleteGroupCommand;
import com.archive.backend.group.app.DeleteGroupHandler;
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
 * Changes of the archive reach the documents of people and groups through the outbox and the worker
 * (docs/features/search.md#synchronization). Every change commits for real; the test drives the worker.
 */
@SpringBootTest
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class})
class LabelOwnerSearchSyncIT {

    private static final Set<GroupTypeCode> DEMOGROUP = Set.of(new GroupTypeCode("demogroup"));

    @Autowired
    private ProcessSearchOutboxHandler worker;

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
    private DeleteGroupHandler deleteGroup;

    @Autowired
    private CreateSoftwareHandler createSoftware;

    @Autowired
    private UpdateSoftwareHandler updateSoftware;

    @Autowired
    private DeleteSoftwareHandler deleteSoftware;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TypesenseContainer typesense;

    private TypesenseProbe probe;

    private final List<UUID> software = new ArrayList<>();

    @BeforeEach
    void setUp() {
        probe = new TypesenseProbe(typesense);
    }

    /** Puts back what the tests committed. */
    @AfterEach
    void putBack() {
        probe.resume();
        software.forEach(id -> jdbc.update("DELETE FROM software WHERE id = ?", id));
        jdbc.update("DELETE FROM label WHERE name IN ('Zed', 'Z', 'ZS', 'Squad')");
        jdbc.update("DELETE FROM person WHERE real_name LIKE 'Zed Zedson%'");
        jdbc.update(
                "DELETE FROM group_group_type WHERE group_id IN (SELECT id FROM \"group\" WHERE name LIKE 'Zed Squad%')");
        jdbc.update("DELETE FROM \"group\" WHERE name LIKE 'Zed Squad%'");
        jdbc.update("DELETE FROM search_outbox");
    }

    private static LabelDraft label(@Nullable LabelId id, String name, boolean display) {
        return new LabelDraft(id, new LabelName(name), display);
    }

    private LabelId labelNamed(String name) {
        return new LabelId(jdbc.queryForObject("SELECT id FROM label WHERE name = ?", UUID.class, name));
    }

    private AuthorId zedZedson() {
        return createAuthor.handle(new CreateAuthorCommand(
                new RealName("Zed Zedson"),
                null,
                List.of(label(null, "Zed", true), label(null, "Z", false)),
                List.of()));
    }

    private GroupId zedSquad() {
        return createGroup.handle(new CreateGroupCommand(
                new GroupName("Zed Squad"),
                null,
                DEMOGROUP,
                List.of(label(null, "ZS", true), label(null, "Squad", false)),
                List.of()));
    }

    private SoftwareId elite(String platform) {
        SoftwareId id = createSoftware.handle(new CreateSoftwareCommand(
                new SoftwareTitle("Elite"), null, null, new PlatformCode(platform), Set.of(), Set.of()));
        software.add(id.value());
        return id;
    }

    /** Credits are not edited in the archive yet: written straight into the table. */
    private void credit(SoftwareId software, String label) {
        jdbc.update(
                "INSERT INTO software_contribution (software_id, label_id) VALUES (?, ?)",
                software.value(),
                labelNamed(label).value());
    }

    private void moveToPlatform(SoftwareId id, String platform) {
        updateSoftware.handle(new UpdateSoftwareCommand(
                id, new SoftwareTitle("Elite"), null, null, new PlatformCode(platform), Set.of(), Set.of()));
    }

    private Map<String, Object> authorDocument(AuthorId id) throws Exception {
        return probe.author(id.value()).orElseThrow(() -> new AssertionError("no document of " + id));
    }

    private Map<String, Object> groupDocument(GroupId id) throws Exception {
        return probe.group(id.value()).orElseThrow(() -> new AssertionError("no document of " + id));
    }

    /** A list field of a document; an absent one is empty. */
    private static List<Object> listIn(Map<String, Object> doc, String field) {
        return doc.get(field) instanceof List<?> list ? List.copyOf(list) : List.of();
    }

    @Test
    void indexesACreatedPersonUnderTheMainNameWithEveryLabelAndTheRealName() throws Exception {
        AuthorId zed = zedZedson();

        worker.handle();

        Map<String, Object> doc = authorDocument(zed);
        assertThat(doc).containsEntry("name", "Zed").containsEntry("real_name", "Zed Zedson");
        assertThat(listIn(doc, "labels")).containsExactlyInAnyOrder("Zed", "Z");
        assertThat(listIn(doc, "platforms")).isEmpty();
    }

    @Test
    void indexesAPersonKnownByTheRealNameAloneUnderIt() throws Exception {
        AuthorId zed =
                createAuthor.handle(new CreateAuthorCommand(new RealName("Zed Zedson"), null, List.of(), List.of()));

        worker.handle();

        assertThat(authorDocument(zed)).containsEntry("name", "Zed Zedson");
    }

    @Test
    void reindexesAnEditedPerson() throws Exception {
        AuthorId zed = zedZedson();
        worker.handle();

        updateAuthor.handle(new UpdateAuthorCommand(
                zed, new RealName("Zed Zedson Jr"), null, List.of(label(labelNamed("Zed"), "Zed", true)), List.of()));
        worker.handle();

        Map<String, Object> doc = authorDocument(zed);
        assertThat(doc).containsEntry("real_name", "Zed Zedson Jr");
        assertThat(listIn(doc, "labels")).containsExactly("Zed");
    }

    @Test
    void removesADeletedPersonFromTheIndex() throws Exception {
        AuthorId zed = zedZedson();
        worker.handle();

        deleteAuthor.handle(new DeleteAuthorCommand(zed));
        worker.handle();

        assertThat(probe.author(zed.value())).isEmpty();
    }

    @Test
    void theAuthorsOfSoftwareTakeItsPlatformWhenTheSoftwareChanges() throws Exception {
        AuthorId zed = zedZedson();
        GroupId squad = zedSquad();
        SoftwareId elite = elite("vector");
        credit(elite, "Z");
        credit(elite, "Squad");

        moveToPlatform(elite, "bk");
        worker.handle();

        assertThat(listIn(authorDocument(zed), "platforms")).containsExactly("bk");
        assertThat(listIn(groupDocument(squad), "platforms")).containsExactly("bk");
    }

    @Test
    void deletingSoftwareTakesItsPlatformFromItsAuthors() throws Exception {
        AuthorId zed = zedZedson();
        GroupId squad = zedSquad();
        SoftwareId elite = elite("vector");
        credit(elite, "Zed");
        credit(elite, "ZS");
        moveToPlatform(elite, "vector");
        worker.handle();
        assertThat(listIn(authorDocument(zed), "platforms")).containsExactly("vector");

        deleteSoftware.handle(new DeleteSoftwareCommand(elite));
        worker.handle();

        assertThat(listIn(authorDocument(zed), "platforms")).isEmpty();
        assertThat(listIn(groupDocument(squad), "platforms")).isEmpty();
    }

    @Test
    void indexesACreatedGroupUnderTheMainNameWithEveryLabelAndItsName() throws Exception {
        GroupId squad = zedSquad();

        worker.handle();

        Map<String, Object> doc = groupDocument(squad);
        assertThat(doc).containsEntry("name", "ZS").containsEntry("group_name", "Zed Squad");
        assertThat(listIn(doc, "labels")).containsExactlyInAnyOrder("ZS", "Squad");
        assertThat(listIn(doc, "platforms")).isEmpty();
    }

    @Test
    void reindexesAnEditedGroup() throws Exception {
        GroupId squad = zedSquad();
        worker.handle();

        updateGroup.handle(new UpdateGroupCommand(
                squad,
                new GroupName("Zed Squad Two"),
                null,
                DEMOGROUP,
                List.of(label(labelNamed("ZS"), "ZS", true)),
                List.of()));
        worker.handle();

        Map<String, Object> doc = groupDocument(squad);
        assertThat(doc).containsEntry("group_name", "Zed Squad Two");
        assertThat(listIn(doc, "labels")).containsExactly("ZS");
    }

    @Test
    void removesADeletedGroupFromTheIndex() throws Exception {
        GroupId squad = zedSquad();
        worker.handle();

        deleteGroup.handle(new DeleteGroupCommand(squad));
        worker.handle();

        assertThat(probe.group(squad.value())).isEmpty();
    }
}
