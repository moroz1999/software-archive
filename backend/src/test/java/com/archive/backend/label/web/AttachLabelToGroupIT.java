package com.archive.backend.label.web;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.IntegrationTest;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.session.AccessTokens;
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
 * A label found out to be another name of a group is made a label of it, keeping its credits and memberships; a label
 * of another group moves to it (docs/domain/authors.md#label).
 */
@IntegrationTest
@Transactional
@Rollback
class AttachLabelToGroupIT {

    private static final UUID GHOSTS = id(1);
    private static final UUID CREW = id(2);
    private static final UUID GHOST = id(3);
    private static final UUID ELITE = id(4);
    private static final UUID JANE = id(5);
    private static final UUID JD = id(6);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccessTokens accessTokens;

    /** Ghosts and Crew, groups; Ghost, a label of nobody, a member of Crew and credited on Elite. */
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
    }

    @Test
    void makesALabelOfNobodyALabelOfTheGroupKeepingItsCreditsAndMemberships() throws Exception {
        attach(GHOST, GHOSTS, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(jsonPath("$.owner.type").value("group"))
                .andExpect(jsonPath("$.owner.id").value(GHOSTS.toString()));
        mvc.perform(get("/api/groups/{id}", GHOSTS))
                .andExpect(jsonPath("$.name").value("Ghosts"))
                .andExpect(jsonPath("$.labels[*].name").value(contains("Ghost")));
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(GHOST.toString())));
        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.members[*].id").value(contains(GHOST.toString())));
    }

    @Test
    void movesALabelOfAnotherGroupToTheGroup() throws Exception {
        jdbc.update("UPDATE label SET group_id = ? WHERE id = ?", CREW, GHOST);

        attach(GHOST, GHOSTS, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.labels").isEmpty());
        mvc.perform(get("/api/groups/{id}", GHOSTS))
                .andExpect(jsonPath("$.labels[*].name").value(contains("Ghost")));
    }

    @Test
    void changesNothingForALabelAlreadyOfTheGroup() throws Exception {
        jdbc.update("UPDATE label SET group_id = ? WHERE id = ?", GHOSTS, GHOST);

        attach(GHOST, GHOSTS, ADMIN).andExpect(status().isNoContent());

        mvc.perform(get("/api/groups/{id}", GHOSTS))
                .andExpect(jsonPath("$.labels[*].name").value(contains("Ghost")));
    }

    @Test
    void refusesALabelOfAPerson() throws Exception {
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Jane Doe')", JANE);
        jdbc.update("INSERT INTO label (id, name, person_id, is_display_name) VALUES (?, 'JD', ?, true)", JD, JANE);

        attach(JD, GHOSTS, ADMIN)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("label.of-person"));
        mvc.perform(get("/api/labels/{id}", JD))
                .andExpect(jsonPath("$.owner.id").value(JANE.toString()));
    }

    @Test
    void refusesAGroupNotInTheArchive() throws Exception {
        attach(GHOST, id(99), ADMIN)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("label.owner-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("group"));
    }

    @Test
    void refusesAnybodyButAnAdministratorEvenWhoeverAddedTheLabel() throws Exception {
        attach(GHOST, GHOSTS, MEMBER).andExpect(status().isForbidden());
        mvc.perform(get("/api/groups/{id}", GHOSTS))
                .andExpect(jsonPath("$.labels").isEmpty());
    }

    @Test
    void needsASignedInUser() throws Exception {
        mvc.perform(post("/api/labels/{id}/attach-to-group", GHOST)
                        .contentType("application/json")
                        .content("{\"group\": \"%s\"}".formatted(GHOSTS)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void attachesNoLabelNotInTheArchive() throws Exception {
        attach(id(98), GHOSTS, ADMIN)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("label.not-found"));
    }

    @Test
    void needsTheGroup() throws Exception {
        mvc.perform(post("/api/labels/{id}/attach-to-group", GHOST)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private ResultActions attach(UUID label, UUID group, Actor actor) throws Exception {
        return mvc.perform(post("/api/labels/{id}/attach-to-group", label)
                .with(TestActors.bearer(accessTokens, actor))
                .contentType("application/json")
                .content("{\"group\": \"%s\"}".formatted(group)));
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00f0-%012d".formatted(number));
    }
}
