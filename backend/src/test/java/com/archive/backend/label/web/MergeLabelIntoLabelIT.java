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
 * A label found out to be a duplicate of another label is merged into it: the other label takes its credits,
 * memberships, locations and origin, and the label goes (docs/domain/authors.md#label).
 */
@IntegrationTest
@Transactional
@Rollback
class MergeLabelIntoLabelIT {

    private static final UUID SPOOK = id(1);
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

    /** Spook, a label of nobody; Crew, a group; Ghost, a label of nobody, a member of Crew, credited on Elite and imported. */
    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        jdbc.update("INSERT INTO label (id, name, is_display_name) VALUES (?, 'Spook', false)", SPOOK);
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
    void givesTheOtherLabelTheCreditsMembershipsAndOriginOfTheLabelAndDeletesIt() throws Exception {
        merge(GHOST, SPOOK, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/labels/{id}", GHOST)).andExpect(status().isNotFound());
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(SPOOK.toString())))
                .andExpect(jsonPath("$.credits[0].type").value("author-label"))
                .andExpect(jsonPath("$.credits[0].roles").value(contains("code")));
        mvc.perform(get("/api/labels/{id}", SPOOK))
                .andExpect(jsonPath("$.groups[*].id").value(contains(CREW.toString())))
                .andExpect(jsonPath("$.origins[*].externalId").value(contains("Ghost")));
        assertThat(memberRoles(SPOOK)).containsExactly("coder");
    }

    @Test
    void joinsTheRolesOfSoftwareCreditingBothLabels() throws Exception {
        jdbc.update(
                "INSERT INTO software_contribution (software_id, label_id, role_codes) VALUES (?, ?, ARRAY['publisher'])",
                ELITE,
                SPOOK);

        merge(GHOST, SPOOK, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(SPOOK.toString())))
                .andExpect(jsonPath("$.credits[0].roles").value(containsInAnyOrder("publisher", "code")));
    }

    @Test
    void keepsTheKnownRolesOfACreditWithoutTheUnknownOne() throws Exception {
        jdbc.update(
                "INSERT INTO software_contribution (software_id, label_id, role_codes) VALUES (?, ?, ARRAY['unknown'])",
                ELITE,
                SPOOK);

        merge(GHOST, SPOOK, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(SPOOK.toString())))
                .andExpect(jsonPath("$.credits[0].roles").value(contains("code")));
    }

    @Test
    void keepsOneMembershipOfTheLabelAlreadyAMemberOfTheSameGroupWithTheRolesOfBoth() throws Exception {
        jdbc.update(
                "INSERT INTO group_member (group_id, label_id, role_codes) VALUES (?, ?, ARRAY['musician'])",
                CREW,
                SPOOK);

        merge(GHOST, SPOOK, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.members[*].id").value(contains(SPOOK.toString())));
        assertThat(memberRoles(SPOOK)).containsExactlyInAnyOrder("musician", "coder");
    }

    @Test
    void keepsTheKnownRolesOfAMembershipWithoutTheUnknownOne() throws Exception {
        jdbc.update(
                "INSERT INTO group_member (group_id, label_id, role_codes) VALUES (?, ?, ARRAY['unknown'])",
                CREW,
                SPOOK);

        merge(GHOST, SPOOK, ADMIN).andExpect(status().isNoContent());

        assertThat(memberRoles(SPOOK)).containsExactly("coder");
    }

    @Test
    void addsTheLocationsOfTheLabelToTheOtherKeepingItsPrimaryAndEachPlaceOnce() throws Exception {
        insertGeo();
        locate(SPOOK, SWEDEN, null, true);
        locate(GHOST, FINLAND, HELSINKI, true);
        locate(GHOST, SWEDEN, null, false);

        merge(GHOST, SPOOK, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/labels/{id}", SPOOK))
                .andExpect(jsonPath("$.locations[*].country").value(contains("Sweden", "Finland")))
                .andExpect(jsonPath("$.locations[*].primary").value(contains(true, false)));
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM entity_location WHERE entity_type = 'LABEL' AND entity_id = ?",
                        Integer.class,
                        GHOST))
                .isZero();
    }

    @Test
    void mergesIntoALabelOfAPerson() throws Exception {
        insertJane();

        merge(GHOST, JD, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(JD.toString())));
        mvc.perform(get("/api/labels/{id}", JD))
                .andExpect(jsonPath("$.owner.id").value(JANE.toString()));
    }

    @Test
    void takesTheLabelAwayFromTheGroupItWasAttachedTo() throws Exception {
        jdbc.update("INSERT INTO label (id, name, group_id, is_display_name) VALUES (?, 'GH', ?, false)", GH, CREW);

        merge(GH, SPOOK, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.labels").isEmpty());
        mvc.perform(get("/api/labels/{id}", GH)).andExpect(status().isNotFound());
    }

    @Test
    void refusesALabelOfAPerson() throws Exception {
        insertJane();

        merge(JD, SPOOK, ADMIN)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("label.of-person"));
        mvc.perform(get("/api/labels/{id}", JD))
                .andExpect(jsonPath("$.owner.id").value(JANE.toString()));
    }

    @Test
    void refusesMergingTheLabelIntoItself() throws Exception {
        merge(GHOST, GHOST, ADMIN)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("label.merge-into-itself"))
                .andExpect(jsonPath("$.errors[0].field").value("label"));
        mvc.perform(get("/api/labels/{id}", GHOST)).andExpect(status().isOk());
    }

    @Test
    void refusesALabelNotInTheArchiveToMergeInto() throws Exception {
        merge(GHOST, id(99), ADMIN)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("label.owner-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("label"));
        mvc.perform(get("/api/labels/{id}", GHOST)).andExpect(status().isOk());
    }

    @Test
    void refusesAnybodyButAnAdministratorEvenWhoeverAddedTheLabel() throws Exception {
        merge(GHOST, SPOOK, MEMBER).andExpect(status().isForbidden());
        mvc.perform(get("/api/labels/{id}", GHOST)).andExpect(status().isOk());
    }

    @Test
    void needsASignedInUser() throws Exception {
        mvc.perform(post("/api/labels/{id}/merge-into-label", GHOST)
                        .contentType("application/json")
                        .content("{\"label\": \"%s\"}".formatted(SPOOK)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mergesNoLabelNotInTheArchive() throws Exception {
        merge(id(98), SPOOK, ADMIN)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("label.not-found"));
    }

    @Test
    void needsTheOtherLabel() throws Exception {
        mvc.perform(post("/api/labels/{id}/merge-into-label", GHOST)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("label.label-required"));
    }

    private ResultActions merge(UUID label, UUID into, Actor actor) throws Exception {
        return mvc.perform(post("/api/labels/{id}/merge-into-label", label)
                .with(TestActors.bearer(accessTokens, actor))
                .contentType("application/json")
                .content("{\"label\": \"%s\"}".formatted(into)));
    }

    private List<String> memberRoles(UUID label) {
        return jdbc.queryForList(
                "SELECT unnest(role_codes) FROM group_member WHERE group_id = ? AND label_id = ?",
                String.class,
                CREW,
                label);
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

    private void locate(UUID label, UUID country, @Nullable UUID city, boolean primary) {
        jdbc.update(
                "INSERT INTO entity_location (entity_type, entity_id, country_id, city_id, is_primary) VALUES ('LABEL', ?, ?, ?, ?)",
                label,
                country,
                city,
                primary);
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00f2-%012d".formatted(number));
    }
}
