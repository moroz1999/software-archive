package com.archive.backend.label.web;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.session.AccessTokens;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** The page of a label of nobody, and attaching it to its owner once it is found out (docs/domain/authors.md#label). */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class})
@Transactional
@Rollback
class LabelIT {

    private static final UUID JANE = id(1);
    private static final UUID JD = id(2);
    private static final UUID CREW = id(3);
    private static final UUID GHOST = id(4);
    private static final UUID ELITE = id(5);

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
        mvc.perform(get("/api/labels/{id}", GHOST).cookie(TestActors.accessCookie(accessTokens, MEMBER)))
                .andExpect(jsonPath("$.canChange").value(false));
        mvc.perform(get("/api/labels/{id}", GHOST).cookie(TestActors.accessCookie(accessTokens, ADMIN)))
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
                        .cookie(TestActors.accessCookie(accessTokens, MEMBER))
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

    private ResultActions attach(UUID label, String type, UUID owner) throws Exception {
        return attach(label, type, owner, false);
    }

    private ResultActions attach(UUID label, String type, UUID owner, boolean display) throws Exception {
        return mvc.perform(post("/api/labels/{id}/owner", label)
                .cookie(TestActors.accessCookie(accessTokens, ADMIN))
                .contentType("application/json")
                .content("{\"type\": \"%s\", \"id\": \"%s\", \"display\": %s}".formatted(type, owner, display)));
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00ee-%012d".formatted(number));
    }
}
