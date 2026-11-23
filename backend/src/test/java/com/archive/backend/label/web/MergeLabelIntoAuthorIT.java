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
import java.util.List;
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
 * A label found out to be a duplicate of a person is merged into them: the person takes its credits, memberships,
 * locations and origin directly, and the label goes (docs/domain/authors.md#label).
 */
@IntegrationTest
@Transactional
@Rollback
class MergeLabelIntoAuthorIT {

    private static final UUID JANE = id(1);
    private static final UUID JD = id(2);
    private static final UUID CREW = id(3);
    private static final UUID GHOST = id(4);
    private static final UUID ELITE = id(5);
    private static final UUID JOHN = id(6);
    private static final UUID JJ = id(7);
    private static final UUID GH = id(8);
    private static final UUID FINLAND = id(9);
    private static final UUID HELSINKI = id(10);
    private static final UUID SWEDEN = id(11);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccessTokens accessTokens;

    /** Jane Doe, known as JD; Crew, a group; Ghost, a label of nobody, a member of Crew, credited on Elite and imported. */
    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Jane Doe')", JANE);
        jdbc.update("INSERT INTO label (id, name, person_id, is_display_name) VALUES (?, 'JD', ?, true)", JD, JANE);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew')", CREW);
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
    void givesThePersonTheCreditsMembershipsAndOriginOfTheLabelAndDeletesIt() throws Exception {
        merge(GHOST, JANE, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/labels/{id}", GHOST)).andExpect(status().isNotFound());
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(JANE.toString())))
                .andExpect(jsonPath("$.credits[0].type").value("author"))
                .andExpect(jsonPath("$.credits[0].roles").value(contains("code")));
        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.members[*].id").value(contains(JANE.toString())))
                .andExpect(jsonPath("$.members[0].type").value("author"));
        assertThat(memberRoles(JANE)).containsExactly("coder");
        mvc.perform(get("/api/authors/{id}", JANE))
                .andExpect(jsonPath("$.labels[*].id").value(contains(JD.toString())));
        assertThat(jdbc.queryForList(
                        "SELECT kind || ' ' || entity_id FROM external_origin WHERE origin = 'vektor-06c.ru' AND external_id = 'Ghost'",
                        String.class))
                .containsExactly("AUTHOR " + JANE);
    }

    @Test
    void joinsTheRolesOfSoftwareCreditingBothTheLabelAndThePerson() throws Exception {
        jdbc.update(
                "INSERT INTO software_contribution (software_id, person_id, role_codes) VALUES (?, ?, ARRAY['publisher'])",
                ELITE,
                JANE);

        merge(GHOST, JANE, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(JANE.toString())))
                .andExpect(jsonPath("$.credits[0].roles").value(containsInAnyOrder("publisher", "code")));
    }

    @Test
    void keepsTheKnownRolesOfACreditWithoutTheUnknownOne() throws Exception {
        jdbc.update(
                "INSERT INTO software_contribution (software_id, person_id, role_codes) VALUES (?, ?, ARRAY['unknown'])",
                ELITE,
                JANE);

        merge(GHOST, JANE, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(JANE.toString())))
                .andExpect(jsonPath("$.credits[0].roles").value(contains("code")));
    }

    @Test
    void keepsOneMembershipOfThePersonAlreadyAMemberOfTheSameGroupWithTheRolesOfBoth() throws Exception {
        jdbc.update(
                "INSERT INTO group_member (group_id, person_id, role_codes) VALUES (?, ?, ARRAY['musician'])",
                CREW,
                JANE);

        merge(GHOST, JANE, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.members[*].id").value(contains(JANE.toString())));
        assertThat(memberRoles(JANE)).containsExactlyInAnyOrder("musician", "coder");
    }

    @Test
    void keepsTheKnownRolesOfAMembershipWithoutTheUnknownOne() throws Exception {
        jdbc.update(
                "INSERT INTO group_member (group_id, person_id, role_codes) VALUES (?, ?, ARRAY['unknown'])",
                CREW,
                JANE);

        merge(GHOST, JANE, ADMIN).andExpect(status().isNoContent());

        assertThat(memberRoles(JANE)).containsExactly("coder");
    }

    @Test
    void addsTheLocationsOfTheLabelToThePersonKeepingTheirPrimaryAndEachPlaceOnce() throws Exception {
        insertGeo();
        locate("PERSON", JANE, SWEDEN, null, true);
        locate("LABEL", GHOST, FINLAND, HELSINKI, true);
        locate("LABEL", GHOST, SWEDEN, null, false);

        merge(GHOST, JANE, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/authors/{id}", JANE))
                .andExpect(jsonPath("$.locations[*].country").value(contains("Sweden", "Finland")))
                .andExpect(jsonPath("$.locations[*].primary").value(contains(true, false)));
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM entity_location WHERE entity_type = 'LABEL' AND entity_id = ?",
                        Integer.class,
                        GHOST))
                .isZero();
    }

    @Test
    void takesTheLabelAwayFromTheGroupItWasAttachedTo() throws Exception {
        jdbc.update("INSERT INTO label (id, name, group_id, is_display_name) VALUES (?, 'GH', ?, false)", GH, CREW);

        merge(GH, JANE, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.labels").isEmpty());
        mvc.perform(get("/api/labels/{id}", GH)).andExpect(status().isNotFound());
    }

    @Test
    void refusesALabelOfAPerson() throws Exception {
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'John Roe')", JOHN);
        jdbc.update("INSERT INTO label (id, name, person_id, is_display_name) VALUES (?, 'JJ', ?, true)", JJ, JOHN);

        merge(JJ, JANE, ADMIN)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("label.of-person"));
        mvc.perform(get("/api/labels/{id}", JJ))
                .andExpect(jsonPath("$.owner.id").value(JOHN.toString()));
    }

    @Test
    void refusesAPersonNotInTheArchive() throws Exception {
        merge(GHOST, id(99), ADMIN)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("label.owner-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("author"));
        mvc.perform(get("/api/labels/{id}", GHOST)).andExpect(status().isOk());
    }

    @Test
    void refusesAnybodyButAnAdministratorEvenWhoeverAddedTheLabel() throws Exception {
        merge(GHOST, JANE, MEMBER).andExpect(status().isForbidden());
        mvc.perform(get("/api/labels/{id}", GHOST)).andExpect(status().isOk());
    }

    @Test
    void needsASignedInUser() throws Exception {
        mvc.perform(post("/api/labels/{id}/merge-into-author", GHOST)
                        .contentType("application/json")
                        .content("{\"author\": \"%s\"}".formatted(JANE)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mergesNoLabelNotInTheArchive() throws Exception {
        merge(id(98), JANE, ADMIN)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("label.not-found"));
    }

    @Test
    void needsThePerson() throws Exception {
        mvc.perform(post("/api/labels/{id}/merge-into-author", GHOST)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("label.author-required"));
    }

    private ResultActions merge(UUID label, UUID author, Actor actor) throws Exception {
        return mvc.perform(post("/api/labels/{id}/merge-into-author", label)
                .with(TestActors.bearer(accessTokens, actor))
                .contentType("application/json")
                .content("{\"author\": \"%s\"}".formatted(author)));
    }

    private List<String> memberRoles(UUID person) {
        return jdbc.queryForList(
                "SELECT unnest(role_codes) FROM group_member WHERE group_id = ? AND person_id = ?",
                String.class,
                CREW,
                person);
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
        return UUID.fromString("00000000-0000-0000-00f1-%012d".formatted(number));
    }
}
