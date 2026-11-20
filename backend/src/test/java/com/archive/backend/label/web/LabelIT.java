package com.archive.backend.label.web;

import static com.archive.backend.geo.GeoTestRows.city;
import static com.archive.backend.geo.GeoTestRows.country;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static com.archive.backend.user.TestActors.OTHER;
import static com.archive.backend.user.TestActors.OTHER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.IntegrationTest;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.session.AccessTokens;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * The page of a label, attaching it to its owner once it is found out, making an owner of it, editing and deleting it
 * (docs/domain/authors.md#label).
 */
@IntegrationTest
@Transactional
@Rollback
class LabelIT {

    private static final UUID JANE = id(1);
    private static final UUID JD = id(2);
    private static final UUID CREW = id(3);
    private static final UUID GHOST = id(4);
    private static final UUID ELITE = id(5);
    private static final UUID JAY = id(6);
    private static final UUID CRW = id(7);
    private static final UUID FINLAND = id(8);
    private static final UUID HELSINKI = id(9);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccessTokens accessTokens;

    /** Jane Doe known as JD; Crew known by its name; Ghost, a label of nobody, a member of Crew and credited on Elite. */
    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Jane Doe')", JANE);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew')", CREW);
        jdbc.update("""
                INSERT INTO label (id, name, person_id, is_display_name) VALUES (?, 'JD', ?, true), (?, 'Ghost', NULL, false)
                """, JD, JANE, GHOST);
        jdbc.update("INSERT INTO group_member (group_id, label_id) VALUES (?, ?)", CREW, GHOST);
        jdbc.update("INSERT INTO software (id, title, platform_code) VALUES (?, 'Elite', 'vector')", ELITE);
        jdbc.update(
                "INSERT INTO software_contribution (software_id, label_id, role_codes) VALUES (?, ?, ARRAY['code'])",
                ELITE,
                GHOST);
        jdbc.update(
                "INSERT INTO external_origin (origin, kind, external_id, entity_id) VALUES ('vektor-06c.ru', 'LABEL', 'Ghost', ?)",
                GHOST);
    }

    @Test
    void showsALabelOfNobodyWithTheGroupsItWasAMemberOf() throws Exception {
        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(GHOST.toString()))
                .andExpect(jsonPath("$.name").value("Ghost"))
                .andExpect(jsonPath("$.owner").value(nullValue()))
                .andExpect(jsonPath("$.groups[*].name").value(contains("Crew")))
                .andExpect(jsonPath("$.groups[0].id").value(CREW.toString()))
                .andExpect(jsonPath("$.canChange").value(false));
    }

    @Test
    void offersTheAttachingToAdministratorsOnly() throws Exception {
        mvc.perform(get("/api/labels/{id}", GHOST).with(TestActors.bearer(accessTokens, MEMBER)))
                .andExpect(jsonPath("$.canChange").value(false));
        mvc.perform(get("/api/labels/{id}", GHOST).with(TestActors.bearer(accessTokens, ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canChange").value(true));
    }

    @Test
    void namesTheOwnerOfALabelThatHasOne() throws Exception {
        mvc.perform(get("/api/labels/{id}", JD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner.type").value("author"))
                .andExpect(jsonPath("$.owner.id").value(JANE.toString()))
                .andExpect(jsonPath("$.owner.name").value("Jane Doe"))
                .andExpect(jsonPath("$.owner.note").value("JD"));
    }

    @Test
    void namesThePersonWithoutARealNameByTheMainName() throws Exception {
        jdbc.update("UPDATE person SET real_name = NULL WHERE id = ?", JANE);

        mvc.perform(get("/api/labels/{id}", JD))
                .andExpect(jsonPath("$.owner.name").value("JD"))
                .andExpect(jsonPath("$.owner.note").value(nullValue()));
    }

    @Test
    void namesTheGroupThatOwnsTheLabelByItsName() throws Exception {
        attach(GHOST, "group", CREW).andExpect(status().isNoContent());

        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner.type").value("group"))
                .andExpect(jsonPath("$.owner.name").value("Crew"))
                .andExpect(jsonPath("$.owner.note").value(nullValue()));
    }

    @Test
    void findsNoLabelThatIsNotInTheArchive() throws Exception {
        mvc.perform(get("/api/labels/{id}", id(99)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("label.not-found"));
    }

    @Test
    void attachesTheLabelToAPersonWithItsCredits() throws Exception {
        attach(GHOST, "author", JANE).andExpect(status().isNoContent());

        mvc.perform(get("/api/authors/{id}", JANE))
                .andExpect(jsonPath("$.name").value("JD"))
                .andExpect(jsonPath("$.labels[*].name").value(contains("Ghost", "JD")));
        mvc.perform(get("/api/authors/{id}/software", JANE))
                .andExpect(jsonPath("$[*].title").value(contains("Elite")))
                .andExpect(jsonPath("$[0].credits[0].as").value("Ghost"));
        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(jsonPath("$.owner.id").value(JANE.toString()));
    }

    @Test
    void attachesTheLabelAsTheDisplayNameWhenToldTo() throws Exception {
        attach(GHOST, "author", JANE, true).andExpect(status().isNoContent());

        mvc.perform(get("/api/authors/{id}", JANE)).andExpect(jsonPath("$.name").value("Ghost"));
    }

    @Test
    void attachesTheLabelToAGroupWithoutLabelsLeavingTheGroupUnderItsName() throws Exception {
        attach(GHOST, "group", CREW).andExpect(status().isNoContent());

        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.name").value("Crew"))
                .andExpect(jsonPath("$.labels[*].name").value(hasItem("Ghost")));
    }

    @Test
    void refusesALabelThatHasAnOwner() throws Exception {
        attach(JD, "group", CREW)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("label.owned"));
    }

    @Test
    void refusesAnOwnerNotInTheArchive() throws Exception {
        attach(GHOST, "author", id(99))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("label.owner-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("owner"));
    }

    @Test
    void findsNoLabelToAttachThatIsNotInTheArchive() throws Exception {
        attach(id(99), "author", JANE)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("label.not-found"));
    }

    @Test
    void refusesWhoeverMayNotChangeTheOwner() throws Exception {
        mvc.perform(post("/api/labels/{id}/owner", GHOST)
                        .with(TestActors.bearer(accessTokens, MEMBER))
                        .contentType("application/json")
                        .content("{\"type\": \"author\", \"id\": \"%s\", \"display\": false}".formatted(JANE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void needsASignedInUser() throws Exception {
        mvc.perform(post("/api/labels/{id}/owner", GHOST)
                        .contentType("application/json")
                        .content("{\"type\": \"author\", \"id\": \"%s\", \"display\": false}".formatted(JANE)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refusesAnOwnerOfAnotherKind() throws Exception {
        attach(GHOST, "author-label", JANE).andExpect(status().isBadRequest());
    }

    @Test
    void offersMakingAnOwnerToWhoeverAddedTheLabelAndAdministrators() throws Exception {
        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(jsonPath("$.canMakeOwner").value(false));
        mvc.perform(get("/api/labels/{id}", GHOST).with(TestActors.bearer(accessTokens, MEMBER)))
                .andExpect(jsonPath("$.canMakeOwner").value(false));
        mvc.perform(get("/api/labels/{id}", GHOST).with(TestActors.bearer(accessTokens, ADMIN)))
                .andExpect(jsonPath("$.canMakeOwner").value(true));

        addedBy(GHOST, MEMBER_ID);

        mvc.perform(get("/api/labels/{id}", GHOST).with(TestActors.bearer(accessTokens, MEMBER)))
                .andExpect(jsonPath("$.canMakeOwner").value(true));
        mvc.perform(get("/api/labels/{id}", GHOST).with(TestActors.bearer(accessTokens, OTHER)))
                .andExpect(jsonPath("$.canMakeOwner").value(false));
    }

    @Test
    void offersMakingAnOwnerOfALabelOfSomebodyOnlyToWhoeverMayChangeThem() throws Exception {
        addedBy(JD, MEMBER_ID);
        jdbc.update("UPDATE person SET added_by = ? WHERE id = ?", OTHER_ID.value(), JANE);

        mvc.perform(get("/api/labels/{id}", JD).with(TestActors.bearer(accessTokens, MEMBER)))
                .andExpect(jsonPath("$.canMakeOwner").value(false));
        mvc.perform(get("/api/labels/{id}", JD).with(TestActors.bearer(accessTokens, ADMIN)))
                .andExpect(jsonPath("$.canMakeOwner").value(true));
    }

    @Test
    void refusesMakingAnOwnerOfALabelSomebodyElseAdded() throws Exception {
        makeAuthor(GHOST, MEMBER).andExpect(status().isForbidden());
        makeGroup(GHOST, MEMBER, "[\"demogroup\"]").andExpect(status().isForbidden());

        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(jsonPath("$.owner").value(nullValue()));
    }

    @Test
    void letsWhoeverAddedTheLabelMakeAnOwnerOfIt() throws Exception {
        addedBy(GHOST, MEMBER_ID);

        makeGroup(GHOST, MEMBER, "[\"demogroup\"]").andExpect(status().isCreated());
    }

    @Test
    void replacesALabelOfNobodyWithAPersonKeepingItsExternalOriginCreditsAndMemberships() throws Exception {
        String id = idOf(makeAuthor(GHOST, ADMIN).andExpect(status().isCreated()));

        mvc.perform(get("/api/authors/{id}", id).with(TestActors.bearer(accessTokens, ADMIN)))
                .andExpect(jsonPath("$.name").value("Ghost"))
                .andExpect(jsonPath("$.realName").value("Ghost"))
                .andExpect(jsonPath("$.labels").isEmpty())
                .andExpect(jsonPath("$.groups[*].name").value(contains("Crew")))
                .andExpect(jsonPath("$.canChange").value(true));
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[0].id").value(id))
                .andExpect(jsonPath("$.credits[0].type").value("author"));
        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.members[0].id").value(id))
                .andExpect(jsonPath("$.members[0].type").value("author"));
        mvc.perform(get("/api/labels/{id}", GHOST)).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject(
                        "SELECT kind FROM external_origin WHERE origin = 'vektor-06c.ru' AND external_id = 'Ghost'",
                        String.class))
                .isEqualTo("AUTHOR");
        assertThat(jdbc.queryForObject(
                        "SELECT entity_id FROM external_origin WHERE origin = 'vektor-06c.ru' AND external_id = 'Ghost'",
                        UUID.class))
                .isEqualTo(UUID.fromString(id));
    }

    @Test
    void replacesALabelWithAPersonLocatedWhereTheLabelWas() throws Exception {
        insertGeo();
        edit(GHOST, ADMIN, """
                        {"name": "Ghost", "owner": null,
                         "locations": [{"country": "%s", "city": "%s", "primary": true}]}
                        """.formatted(FINLAND, HELSINKI)).andExpect(status().isNoContent());

        String id = idOf(makeAuthor(GHOST, ADMIN).andExpect(status().isCreated()));

        mvc.perform(get("/api/authors/{id}", id))
                .andExpect(jsonPath("$.locations[*].city").value(contains("Helsinki")))
                .andExpect(jsonPath("$.locations[0].primary").value(true));
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM entity_location WHERE entity_type = 'LABEL' AND entity_id = ?",
                        Integer.class,
                        GHOST))
                .isZero();
    }

    @Test
    void makesAGroupOfALabelOfNobodyUnderItsNameWithItsCreditsAndMemberships() throws Exception {
        String id = idOf(makeGroup(GHOST, ADMIN, "[\"demogroup\"]").andExpect(status().isCreated()));

        mvc.perform(get("/api/groups/{id}", id).with(TestActors.bearer(accessTokens, ADMIN)))
                .andExpect(jsonPath("$.name").value("Ghost"))
                .andExpect(jsonPath("$.types").value(contains("demogroup")))
                .andExpect(jsonPath("$.labels[*].name").value(contains("Ghost")))
                .andExpect(jsonPath("$.canChange").value(true));
        mvc.perform(get("/api/groups/{id}/software", id))
                .andExpect(jsonPath("$[*].title").value(contains("Elite")));
        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.members[*].name").value(contains("Ghost")));
        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(jsonPath("$.owner.type").value("group"))
                .andExpect(jsonPath("$.owner.id").value(id));
    }

    @Test
    void replacesALabelTakenAwayFromItsPerson() throws Exception {
        jdbc.update("INSERT INTO label (id, name, person_id, is_display_name) VALUES (?, 'Jay', ?, false)", JAY, JANE);

        String id = idOf(makeAuthor(JAY, ADMIN).andExpect(status().isCreated()));

        mvc.perform(get("/api/authors/{id}", JANE))
                .andExpect(jsonPath("$.labels[*].name").value(contains("JD")));
        mvc.perform(get("/api/authors/{id}", id))
                .andExpect(jsonPath("$.realName").value("Jay"))
                .andExpect(jsonPath("$.labels").isEmpty());
        mvc.perform(get("/api/labels/{id}", JAY)).andExpect(status().isNotFound());
    }

    @Test
    void takesTheLabelAwayFromTheGroupItWasAttachedTo() throws Exception {
        jdbc.update("INSERT INTO label (id, name, group_id, is_display_name) VALUES (?, 'CRW', ?, false)", CRW, CREW);

        String id = idOf(makeGroup(CRW, ADMIN, "[\"label\"]").andExpect(status().isCreated()));

        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.labels").isEmpty());
        mvc.perform(get("/api/groups/{id}", id)).andExpect(jsonPath("$.name").value("CRW"));
    }

    @Test
    void keepsTheDisplayNameOfAPersonWithOtherLabels() throws Exception {
        jdbc.update("INSERT INTO label (id, name, person_id, is_display_name) VALUES (?, 'Jay', ?, false)", JAY, JANE);

        makeAuthor(JD, ADMIN)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("label.display-one"));
        mvc.perform(get("/api/authors/{id}", JANE))
                .andExpect(jsonPath("$.labels[*].name").value(contains("Jay", "JD")));
    }

    @Test
    void keepsTheOnlyLabelOfAPersonWithoutARealName() throws Exception {
        jdbc.update("UPDATE person SET real_name = NULL WHERE id = ?", JANE);

        makeGroup(JD, ADMIN, "[\"demogroup\"]")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("author.nameless"));
    }

    @Test
    void refusesTakingALabelFromAnOwnerTheUserMayNotChange() throws Exception {
        jdbc.update("INSERT INTO label (id, name, person_id, is_display_name) VALUES (?, 'Jay', ?, false)", JAY, JANE);
        addedBy(JAY, MEMBER_ID);

        makeAuthor(JAY, MEMBER).andExpect(status().isForbidden());
        mvc.perform(get("/api/labels/{id}", JAY))
                .andExpect(jsonPath("$.owner.id").value(JANE.toString()));
    }

    @Test
    void makesNoOwnerForAPublicVisitor() throws Exception {
        mvc.perform(post("/api/labels/{id}/author", GHOST)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/labels/{id}/group", GHOST)
                        .contentType("application/json")
                        .content("{\"types\": [\"demogroup\"]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void makesNoOwnerOfALabelNotInTheArchive() throws Exception {
        makeAuthor(id(99), ADMIN)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("label.not-found"));
        makeGroup(id(99), ADMIN, "[\"demogroup\"]")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("label.not-found"));
    }

    @Test
    void makesNoGroupWithoutAKnownType() throws Exception {
        makeGroup(GHOST, ADMIN, "[\"nonsense\"]")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("group.type-unknown"));
        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(jsonPath("$.owner").value(nullValue()));
    }

    @Test
    void showsTheLocationsAndTheOriginsOfALabel() throws Exception {
        insertGeo();
        jdbc.update(
                "INSERT INTO entity_location (entity_type, entity_id, country_id, city_id, is_primary) VALUES ('LABEL', ?, ?, ?, true)",
                GHOST,
                FINLAND,
                HELSINKI);
        jdbc.update(
                "INSERT INTO external_origin (origin, kind, external_id, entity_id, url) VALUES ('zx.example', 'LABEL', '42', ?, 'https://zx.example/42')",
                GHOST);

        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locations[0].countryId").value(FINLAND.toString()))
                .andExpect(jsonPath("$.locations[0].country").value("Finland"))
                .andExpect(jsonPath("$.locations[0].cityId").value(HELSINKI.toString()))
                .andExpect(jsonPath("$.locations[0].city").value("Helsinki"))
                .andExpect(jsonPath("$.locations[0].primary").value(true))
                .andExpect(jsonPath("$.origins[*].origin").value(contains("vektor-06c.ru", "zx.example")))
                .andExpect(jsonPath("$.origins[0].externalId").value("Ghost"))
                .andExpect(jsonPath("$.origins[0].url").value(nullValue()))
                .andExpect(jsonPath("$.origins[1].externalId").value("42"))
                .andExpect(jsonPath("$.origins[1].url").value("https://zx.example/42"));
    }

    @Test
    void namesTheLocationsOfALabelInTheLanguageAskedFor() throws Exception {
        insertGeo();
        jdbc.update(
                "INSERT INTO entity_location (entity_type, entity_id, country_id, city_id, is_primary) VALUES ('LABEL', ?, ?, ?, true)",
                GHOST,
                FINLAND,
                HELSINKI);

        mvc.perform(get("/api/labels/{id}", GHOST).header("Accept-Language", "ru"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locations[0].country").value("Финляндия"))
                .andExpect(jsonPath("$.locations[0].city").value("Хельсинки"));
    }

    @Test
    void offersEditingToWhoeverAddedTheLabelAndAdministrators() throws Exception {
        addedBy(GHOST, MEMBER_ID);

        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(jsonPath("$.canEdit").value(false))
                .andExpect(jsonPath("$.canChangeOwner").value(false));
        mvc.perform(get("/api/labels/{id}", GHOST).with(TestActors.bearer(accessTokens, MEMBER)))
                .andExpect(jsonPath("$.canEdit").value(true))
                .andExpect(jsonPath("$.canChangeOwner").value(false));
        mvc.perform(get("/api/labels/{id}", GHOST).with(TestActors.bearer(accessTokens, OTHER)))
                .andExpect(jsonPath("$.canEdit").value(false));
        mvc.perform(get("/api/labels/{id}", GHOST).with(TestActors.bearer(accessTokens, ADMIN)))
                .andExpect(jsonPath("$.canEdit").value(true))
                .andExpect(jsonPath("$.canChangeOwner").value(true));
    }

    @Test
    void remembersWhoTypedALabelInTheFormOfAPerson() throws Exception {
        mvc.perform(post("/api/authors")
                        .with(TestActors.bearer(accessTokens, MEMBER))
                        .contentType("application/json")
                        .content("{\"labels\": [{\"name\": \"Neo\", \"display\": true}]}"))
                .andExpect(status().isCreated());
        UUID neo = jdbc.queryForObject("SELECT id FROM label WHERE name = 'Neo'", UUID.class);

        mvc.perform(get("/api/labels/{id}", neo).with(TestActors.bearer(accessTokens, MEMBER)))
                .andExpect(jsonPath("$.canEdit").value(true));
        mvc.perform(get("/api/labels/{id}", neo).with(TestActors.bearer(accessTokens, OTHER)))
                .andExpect(jsonPath("$.canEdit").value(false));
    }

    @Test
    void renamesTheLabelKeepingItsCreditsAndMemberships() throws Exception {
        edit(GHOST, ADMIN, "{\"name\": \"Spook\", \"owner\": null, \"locations\": []}")
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(jsonPath("$.name").value("Spook"))
                .andExpect(jsonPath("$.groups[*].name").value(contains("Crew")));
        mvc.perform(get("/api/labels/{id}/software", GHOST))
                .andExpect(jsonPath("$[*].title").value(contains("Elite")));
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM search_outbox WHERE entity_type = 'SOFTWARE' AND entity_id = ?",
                        Integer.class,
                        ELITE))
                .isEqualTo(1);
    }

    @Test
    void locatesTheLabel() throws Exception {
        insertGeo();

        edit(GHOST, ADMIN, """
                        {"name": "Ghost", "owner": null,
                         "locations": [{"country": "%s", "city": "%s", "primary": true}]}
                        """.formatted(FINLAND, HELSINKI)).andExpect(status().isNoContent());

        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(jsonPath("$.locations[*].city").value(contains("Helsinki")));
    }

    @Test
    void keepsTheLocationsOfALabelWhenItsOwnerIsSaved() throws Exception {
        insertGeo();
        edit(JD, ADMIN, """
                        {"name": "JD", "owner": {"type": "author", "id": "%s", "display": true},
                         "locations": [{"country": "%s", "city": null, "primary": true}]}
                        """.formatted(JANE, FINLAND)).andExpect(status().isNoContent());

        mvc.perform(put("/api/authors/{id}", JANE)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content(
                                "{\"realName\": \"Jane Doe\", \"labels\": [{\"id\": \"%s\", \"name\": \"JD\", \"display\": true}]}"
                                        .formatted(JD)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/labels/{id}", JD))
                .andExpect(jsonPath("$.locations[*].country").value(contains("Finland")));
    }

    @Test
    void letsWhoeverAddedTheLabelEditIt() throws Exception {
        addedBy(GHOST, MEMBER_ID);

        edit(GHOST, MEMBER, "{\"name\": \"Spook\", \"owner\": null, \"locations\": []}")
                .andExpect(status().isNoContent());
    }

    @Test
    void refusesEditingByAnybodyElse() throws Exception {
        addedBy(GHOST, OTHER_ID);

        edit(GHOST, MEMBER, "{\"name\": \"Spook\", \"owner\": null, \"locations\": []}")
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/labels/{id}", GHOST)
                        .contentType("application/json")
                        .content("{\"name\": \"Spook\", \"owner\": null, \"locations\": []}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/labels/{id}", GHOST)).andExpect(jsonPath("$.name").value("Ghost"));
    }

    @Test
    void attachesTheLabelToAPersonAsTheDisplayNameThroughTheForm() throws Exception {
        edit(GHOST, ADMIN, """
                        {"name": "Ghost", "owner": {"type": "author", "id": "%s", "display": true}, "locations": []}
                        """.formatted(JANE)).andExpect(status().isNoContent());

        mvc.perform(get("/api/authors/{id}", JANE))
                .andExpect(jsonPath("$.name").value("Ghost"))
                .andExpect(jsonPath("$.labels[*].name").value(contains("Ghost", "JD")));
    }

    @Test
    void movesTheLabelToAnotherOwner() throws Exception {
        edit(JD, ADMIN, """
                        {"name": "JD", "owner": {"type": "group", "id": "%s", "display": false}, "locations": []}
                        """.formatted(CREW)).andExpect(status().isNoContent());

        mvc.perform(get("/api/authors/{id}", JANE))
                .andExpect(jsonPath("$.labels").isEmpty());
        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.labels[*].name").value(contains("JD")));
    }

    @Test
    void leavesTheLabelToNobody() throws Exception {
        edit(JD, ADMIN, "{\"name\": \"JD\", \"owner\": null, \"locations\": []}")
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/labels/{id}", JD)).andExpect(jsonPath("$.owner").value(nullValue()));
    }

    @Test
    void keepsTheOwnerWhenTheSameOneIsSent() throws Exception {
        addedBy(JD, MEMBER_ID);

        edit(JD, MEMBER, """
                        {"name": "J.D.", "owner": {"type": "author", "id": "%s", "display": false}, "locations": []}
                        """.formatted(JANE)).andExpect(status().isNoContent());

        mvc.perform(get("/api/authors/{id}", JANE))
                .andExpect(jsonPath("$.name").value("J.D."))
                .andExpect(jsonPath("$.labels[0].display").value(true));
    }

    @Test
    void refusesChangingTheOwnerByAnybodyButAnAdministrator() throws Exception {
        addedBy(GHOST, MEMBER_ID);

        edit(GHOST, MEMBER, """
                        {"name": "Ghost", "owner": {"type": "author", "id": "%s", "display": false}, "locations": []}
                        """.formatted(JANE)).andExpect(status().isForbidden());
        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(jsonPath("$.owner").value(nullValue()));
    }

    @Test
    void refusesAnOwnerNotInTheArchiveUnderTheOwner() throws Exception {
        edit(GHOST, ADMIN, """
                        {"name": "Ghost", "owner": {"type": "author", "id": "%s", "display": false}, "locations": []}
                        """.formatted(id(99)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("label.owner-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("owner"));
    }

    @Test
    void refusesTakingTheDisplayNameFromAPersonWithOtherLabels() throws Exception {
        jdbc.update("INSERT INTO label (id, name, person_id, is_display_name) VALUES (?, 'Jay', ?, false)", JAY, JANE);

        edit(JD, ADMIN, "{\"name\": \"JD\", \"owner\": null, \"locations\": []}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("label.display-one"))
                .andExpect(jsonPath("$.errors[0].field").value("owner"));
        mvc.perform(get("/api/labels/{id}", JD))
                .andExpect(jsonPath("$.owner.id").value(JANE.toString()));
    }

    @Test
    void refusesTakingTheOnlyNameOfAPersonWithoutARealName() throws Exception {
        jdbc.update("UPDATE person SET real_name = NULL WHERE id = ?", JANE);

        edit(JD, ADMIN, "{\"name\": \"JD\", \"owner\": null, \"locations\": []}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("author.nameless"))
                .andExpect(jsonPath("$.errors[0].field").value("owner"));
    }

    @Test
    void refusesAPlaceNotInTheArchiveUnderTheLocations() throws Exception {
        edit(GHOST, ADMIN, """
                        {"name": "Ghost", "owner": null,
                         "locations": [{"country": "%s", "city": null, "primary": true}]}
                        """.formatted(id(98)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("location.unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("locations"));
    }

    @Test
    void refusesABlankName() throws Exception {
        edit(GHOST, ADMIN, "{\"name\": \" \", \"owner\": null, \"locations\": []}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("title.required"));
    }

    @Test
    void editsNoLabelThatIsNotInTheArchive() throws Exception {
        edit(id(99), ADMIN, "{\"name\": \"Spook\", \"owner\": null, \"locations\": []}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("label.not-found"));
    }

    @Test
    void deletesTheLabelWithItsCreditsMembershipsLocationsAndOrigin() throws Exception {
        insertGeo();
        jdbc.update(
                "INSERT INTO entity_location (entity_type, entity_id, country_id, is_primary) VALUES ('LABEL', ?, ?, true)",
                GHOST,
                FINLAND);

        remove(GHOST, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/labels/{id}", GHOST)).andExpect(status().isNotFound());
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits").isEmpty());
        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.members").isEmpty());
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM entity_location WHERE entity_id = ?", Integer.class, GHOST))
                .isZero();
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM external_origin WHERE entity_id = ?", Integer.class, GHOST))
                .isZero();
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM search_outbox WHERE entity_type = 'SOFTWARE' AND entity_id = ?",
                        Integer.class,
                        ELITE))
                .isEqualTo(1);
    }

    @Test
    void deletesALabelOfAPerson() throws Exception {
        jdbc.update("INSERT INTO label (id, name, person_id, is_display_name) VALUES (?, 'Jay', ?, false)", JAY, JANE);

        remove(JAY, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/authors/{id}", JANE))
                .andExpect(jsonPath("$.labels[*].name").value(contains("JD")));
    }

    @Test
    void letsWhoeverAddedTheLabelDeleteIt() throws Exception {
        addedBy(GHOST, MEMBER_ID);

        remove(GHOST, MEMBER).andExpect(status().isNoContent());
    }

    @Test
    void keepsTheDisplayNameOfAPersonWithOtherLabelsFromDeleting() throws Exception {
        jdbc.update("INSERT INTO label (id, name, person_id, is_display_name) VALUES (?, 'Jay', ?, false)", JAY, JANE);

        remove(JD, ADMIN)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("label.display-one"));
        mvc.perform(get("/api/labels/{id}", JD)).andExpect(status().isOk());
    }

    @Test
    void keepsTheOnlyNameOfAPersonWithoutARealNameFromDeleting() throws Exception {
        jdbc.update("UPDATE person SET real_name = NULL WHERE id = ?", JANE);

        remove(JD, ADMIN)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("author.nameless"));
    }

    @Test
    void refusesDeletingByAnybodyElse() throws Exception {
        addedBy(GHOST, OTHER_ID);

        remove(GHOST, MEMBER).andExpect(status().isForbidden());
        mvc.perform(delete("/api/labels/{id}", GHOST)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/labels/{id}", GHOST)).andExpect(status().isOk());
    }

    @Test
    void deletesNoLabelThatIsNotInTheArchive() throws Exception {
        remove(id(99), ADMIN)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("label.not-found"));
    }

    @Test
    void addsALabelOfNobodyWithItsLocationsAddedByWhoeverGaveIt() throws Exception {
        insertGeo();

        ResultActions created = create(MEMBER, """
                        {"name": "Ghost",
                         "locations": [{"country": "%s", "city": "%s", "primary": true}]}
                        """.formatted(FINLAND, HELSINKI)).andExpect(status().isCreated());
        String id = idOf(created);

        assertThat(created.andReturn().getResponse().getHeader("Location")).isEqualTo("/api/labels/" + id);
        mvc.perform(get("/api/labels/{id}", id).with(TestActors.bearer(accessTokens, MEMBER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ghost"))
                .andExpect(jsonPath("$.owner").value(nullValue()))
                .andExpect(jsonPath("$.locations[*].city").value(contains("Helsinki")))
                .andExpect(jsonPath("$.added.by").value("TestMember"))
                .andExpect(jsonPath("$.canEdit").value(true));
        mvc.perform(get("/api/labels/{id}", id).with(TestActors.bearer(accessTokens, OTHER)))
                .andExpect(jsonPath("$.canEdit").value(false));
    }

    @Test
    void addsALabelOfTheSameNameAsAnotherAsALabelOfItsOwn() throws Exception {
        String id =
                idOf(create(MEMBER, "{\"name\": \"Ghost\", \"locations\": []}").andExpect(status().isCreated()));

        assertThat(id).isNotEqualTo(GHOST.toString());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM label WHERE name = 'Ghost'", Integer.class))
                .isEqualTo(2);
        mvc.perform(get("/api/labels/{id}", id)).andExpect(jsonPath("$.groups").isEmpty());
    }

    @Test
    void putsTheNewLabelInTheAuthorCatalog() throws Exception {
        create(MEMBER, "{\"name\": \"Zork\", \"locations\": []}").andExpect(status().isCreated());

        mvc.perform(get("/api/authors").param("letter", "Z"))
                .andExpect(jsonPath("$.content[*].name").value(contains("Zork")));
    }

    @Test
    void addsNoLabelForAPublicVisitor() throws Exception {
        mvc.perform(post("/api/labels")
                        .contentType("application/json")
                        .content("{\"name\": \"Zork\", \"locations\": []}"))
                .andExpect(status().isUnauthorized());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM label WHERE name = 'Zork'", Integer.class))
                .isZero();
    }

    @Test
    void addsNoLabelWithoutAName() throws Exception {
        create(MEMBER, "{\"name\": \" \", \"locations\": []}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("title.required"));
        create(MEMBER, "{\"name\": \"%s\", \"locations\": []}".formatted("x".repeat(256)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("title.too-long"));
    }

    @Test
    void addsNoLabelWithALocationWithoutAPlace() throws Exception {
        create(MEMBER, "{\"name\": \"Zork\", \"locations\": [{\"country\": null, \"city\": null, \"primary\": true}]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("location.empty"));
    }

    @Test
    void refusesAPlaceNotInTheArchiveUnderTheLocationsOfANewLabel() throws Exception {
        create(MEMBER, """
                        {"name": "Zork", "locations": [{"country": "%s", "city": null, "primary": true}]}
                        """.formatted(id(98)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("location.unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("locations"));
    }

    @Test
    void refusesANewLabelWithoutExactlyOnePrimaryLocation() throws Exception {
        insertGeo();

        create(MEMBER, """
                        {"name": "Zork", "locations": [{"country": "%s", "city": null, "primary": false}]}
                        """.formatted(FINLAND))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("location.primary-one"))
                .andExpect(jsonPath("$.errors[0].field").value("locations"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM label WHERE name = 'Zork'", Integer.class))
                .isZero();
    }

    private ResultActions create(Actor actor, String body) throws Exception {
        return mvc.perform(post("/api/labels")
                .with(TestActors.bearer(accessTokens, actor))
                .contentType("application/json")
                .content(body));
    }

    private void addedBy(UUID label, UserId user) {
        jdbc.update("UPDATE label SET added_by = ? WHERE id = ?", user.value(), label);
    }

    private void insertGeo() {
        country(jdbc, FINLAND, "Финляндия", "Finland");
        city(jdbc, HELSINKI, FINLAND, "Хельсинки", "Helsinki");
    }

    private ResultActions edit(UUID label, Actor actor, String body) throws Exception {
        return mvc.perform(put("/api/labels/{id}", label)
                .with(TestActors.bearer(accessTokens, actor))
                .contentType("application/json")
                .content(body));
    }

    private ResultActions remove(UUID label, Actor actor) throws Exception {
        return mvc.perform(delete("/api/labels/{id}", label).with(TestActors.bearer(accessTokens, actor)));
    }

    private ResultActions makeAuthor(UUID label, Actor actor) throws Exception {
        return mvc.perform(post("/api/labels/{id}/author", label).with(TestActors.bearer(accessTokens, actor)));
    }

    private ResultActions makeGroup(UUID label, Actor actor, String types) throws Exception {
        return mvc.perform(post("/api/labels/{id}/group", label)
                .with(TestActors.bearer(accessTokens, actor))
                .contentType("application/json")
                .content("{\"types\": %s}".formatted(types)));
    }

    private static String idOf(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
    }

    private ResultActions attach(UUID label, String type, UUID owner) throws Exception {
        return attach(label, type, owner, false);
    }

    private ResultActions attach(UUID label, String type, UUID owner, boolean display) throws Exception {
        return mvc.perform(post("/api/labels/{id}/owner", label)
                .with(TestActors.bearer(accessTokens, ADMIN))
                .contentType("application/json")
                .content("{\"type\": \"%s\", \"id\": \"%s\", \"display\": %s}".formatted(type, owner, display)));
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00ee-%012d".formatted(number));
    }
}
