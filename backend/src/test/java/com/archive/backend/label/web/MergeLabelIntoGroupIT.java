package com.archive.backend.label.web;

import static com.archive.backend.geo.GeoTestRows.city;
import static com.archive.backend.geo.GeoTestRows.country;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.IntegrationTest;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.session.AccessTokens;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * A label found out to be a duplicate of a group is merged into it: the group takes its credits, memberships,
 * locations and origin directly, and the label goes (docs/domain/authors.md#label).
 */
@IntegrationTest
@Transactional
@Rollback
class MergeLabelIntoGroupIT {

    private static final UUID GHOSTS = id(1);
    private static final UUID CREW = id(2);
    private static final UUID GHOST = id(3);
    private static final UUID ELITE = id(4);
    private static final UUID JANE = id(5);
    private static final UUID JD = id(6);
    private static final UUID GH = id(7);
    private static final UUID FINLAND = id(8);
    private static final UUID HELSINKI = id(9);
    private static final UUID SWEDEN = id(10);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccessTokens accessTokens;

    /** Ghosts and Crew, groups; Ghost, a label of nobody, a member of Crew, credited on Elite and imported. */
    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Ghosts'), (?, 'Crew')", GHOSTS, CREW);
        jdbc.update("INSERT INTO label (id, name, is_display_name) VALUES (?, 'Ghost', false)", GHOST);
        jdbc.update("UPDATE label SET added_by = ? WHERE id = ?", MEMBER_ID.value(), GHOST);
        jdbc.update(
                "INSERT INTO group_member (group_id, label_id, role_codes) VALUES (?, ?, ARRAY['coder'])", CREW, GHOST);
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
    void offersHandingTheLabelOverToAdministratorsOnly() throws Exception {
        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(jsonPath("$.canHandOver").value(false));
        mvc.perform(get("/api/labels/{id}", GHOST).with(TestActors.bearer(accessTokens, MEMBER)))
                .andExpect(jsonPath("$.canHandOver").value(false));
        mvc.perform(get("/api/labels/{id}", GHOST).with(TestActors.bearer(accessTokens, ADMIN)))
                .andExpect(jsonPath("$.canHandOver").value(true));
    }

    @Test
    void offersNoHandingOverOfALabelOfAPerson() throws Exception {
        insertJane();

        mvc.perform(get("/api/labels/{id}", JD).with(TestActors.bearer(accessTokens, ADMIN)))
                .andExpect(jsonPath("$.canHandOver").value(false));
    }

    @Test
    void givesTheGroupTheCreditsMembershipsAndOriginOfTheLabelAndDeletesIt() throws Exception {
        merge(GHOST, GHOSTS, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/labels/{id}", GHOST)).andExpect(status().isNotFound());
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(GHOSTS.toString())))
                .andExpect(jsonPath("$.credits[0].type").value("group"))
                .andExpect(jsonPath("$.credits[0].roles").value(contains("code")));
        // A group member has no roles: the membership of the label becomes Ghosts as a member group of Crew.
        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.members").isEmpty())
                .andExpect(jsonPath("$.memberGroups[*].id").value(contains(GHOSTS.toString())));
        assertThat(jdbc.queryForList(
                        "SELECT kind || ' ' || entity_id FROM external_origin WHERE origin = 'vektor-06c.ru' AND external_id = 'Ghost'",
                        String.class))
                .containsExactly("GROUP " + GHOSTS);
    }

    @Test
    void joinsTheRolesOfSoftwareCreditingBothTheLabelAndTheGroup() throws Exception {
        jdbc.update(
                "INSERT INTO software_contribution (software_id, group_id, role_codes) VALUES (?, ?, ARRAY['publisher'])",
                ELITE,
                GHOSTS);

        merge(GHOST, GHOSTS, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(GHOSTS.toString())))
                .andExpect(jsonPath("$.credits[0].roles").value(containsInAnyOrder("publisher", "code")));
    }

    @Test
    void keepsTheKnownRolesOfACreditWithoutTheUnknownOne() throws Exception {
        jdbc.update(
                "INSERT INTO software_contribution (software_id, group_id, role_codes) VALUES (?, ?, ARRAY['unknown'])",
                ELITE,
                GHOSTS);

        merge(GHOST, GHOSTS, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(GHOSTS.toString())))
                .andExpect(jsonPath("$.credits[0].roles").value(contains("code")));
    }

    @Test
    void keepsOneMembershipOfTheGroupAlreadyAMemberOfTheSameGroup() throws Exception {
        jdbc.update("INSERT INTO group_member_group (group_id, member_group_id) VALUES (?, ?)", CREW, GHOSTS);

        merge(GHOST, GHOSTS, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.members").isEmpty())
                .andExpect(jsonPath("$.memberGroups[*].id").value(contains(GHOSTS.toString())));
    }

    @Test
    void dropsTheMembershipOfTheLabelInTheGroupItIsMergedInto() throws Exception {
        merge(GHOST, CREW, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.members").isEmpty())
                .andExpect(jsonPath("$.memberGroups").isEmpty())
                .andExpect(jsonPath("$.groups").isEmpty());
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(CREW.toString())));
    }

    @Test
    void addsTheLocationsOfTheLabelToTheGroupKeepingItsPrimaryAndEachPlaceOnce() throws Exception {
        insertGeo();
        locate("GROUP", GHOSTS, SWEDEN, null, true);
        locate("LABEL", GHOST, FINLAND, HELSINKI, true);
        locate("LABEL", GHOST, SWEDEN, null, false);

        merge(GHOST, GHOSTS, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/groups/{id}", GHOSTS))
                .andExpect(jsonPath("$.locations[*].country").value(contains("Sweden", "Finland")))
                .andExpect(jsonPath("$.locations[*].primary").value(contains(true, false)));
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM entity_location WHERE entity_type = 'LABEL' AND entity_id = ?",
                        Integer.class,
                        GHOST))
                .isZero();
    }

    @Test
    void locatesAGroupWithoutLocationsWhereTheLabelWas() throws Exception {
        insertGeo();
        locate("LABEL", GHOST, FINLAND, HELSINKI, true);

        merge(GHOST, GHOSTS, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/groups/{id}", GHOSTS))
                .andExpect(jsonPath("$.locations[*].city").value(contains("Helsinki")))
                .andExpect(jsonPath("$.locations[0].primary").value(true));
    }

    @Test
    void takesTheLabelAwayFromTheGroupItWasAttachedTo() throws Exception {
        jdbc.update("INSERT INTO label (id, name, group_id, is_display_name) VALUES (?, 'GH', ?, false)", GH, CREW);

        merge(GH, GHOSTS, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.labels").isEmpty());
        mvc.perform(get("/api/labels/{id}", GH)).andExpect(status().isNotFound());
    }

    @Test
    void mergesALabelOfTheGroupIntoItself() throws Exception {
        jdbc.update("UPDATE label SET group_id = ? WHERE id = ?", GHOSTS, GHOST);

        merge(GHOST, GHOSTS, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/groups/{id}", GHOSTS))
                .andExpect(jsonPath("$.name").value("Ghosts"))
                .andExpect(jsonPath("$.labels").isEmpty());
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(GHOSTS.toString())));
    }

    @Test
    void refusesALabelOfAPerson() throws Exception {
        insertJane();

        merge(JD, GHOSTS, ADMIN)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("label.of-person"));
        mvc.perform(get("/api/labels/{id}", JD))
                .andExpect(jsonPath("$.owner.id").value(JANE.toString()));
    }

    @Test
    void refusesAGroupNotInTheArchive() throws Exception {
        merge(GHOST, id(99), ADMIN)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("label.owner-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("group"));
        mvc.perform(get("/api/labels/{id}", GHOST)).andExpect(status().isOk());
    }

    @Test
    void refusesAnybodyButAnAdministratorEvenWhoeverAddedTheLabel() throws Exception {
        merge(GHOST, GHOSTS, MEMBER).andExpect(status().isForbidden());
        mvc.perform(get("/api/labels/{id}", GHOST)).andExpect(status().isOk());
    }

    @Test
    void needsASignedInUser() throws Exception {
        mvc.perform(post("/api/labels/{id}/merge-into-group", GHOST)
                        .contentType("application/json")
                        .content("{\"group\": \"%s\"}".formatted(GHOSTS)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mergesNoLabelNotInTheArchive() throws Exception {
        merge(id(98), GHOSTS, ADMIN)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("label.not-found"));
    }

    @Test
    void needsTheGroup() throws Exception {
        mvc.perform(post("/api/labels/{id}/merge-into-group", GHOST)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private ResultActions merge(UUID label, UUID group, Actor actor) throws Exception {
        return mvc.perform(post("/api/labels/{id}/merge-into-group", label)
                .with(TestActors.bearer(accessTokens, actor))
                .contentType("application/json")
                .content("{\"group\": \"%s\"}".formatted(group)));
    }

    private void insertJane() {
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Jane Doe')", JANE);
        jdbc.update("INSERT INTO label (id, name, person_id, is_display_name) VALUES (?, 'JD', ?, true)", JD, JANE);
    }

    private void insertGeo() {
        country(jdbc, FINLAND, "Финляндия", "Finland");
        city(jdbc, HELSINKI, FINLAND, "Хельсинки", "Helsinki");
        country(jdbc, SWEDEN, "Швеция", "Sweden");
    }

    private void locate(String type, UUID entity, UUID country, @Nullable UUID city, boolean primary) {
        jdbc.update(
                "INSERT INTO entity_location (entity_type, entity_id, country_id, city_id, is_primary) VALUES (?, ?, ?, ?, ?)",
                type,
                entity,
                country,
                city,
                primary);
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00ef-%012d".formatted(number));
    }
}
