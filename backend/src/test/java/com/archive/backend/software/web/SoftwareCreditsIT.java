package com.archive.backend.software.web;

import static com.archive.backend.user.TestActors.MEMBER;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.IntegrationTest;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.session.AccessTokens;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Credits of people, groups and their labels chosen while software is added or edited. */
@IntegrationTest
@Transactional
@Rollback
class SoftwareCreditsIT {

    private static final UUID AUTHOR = id(1);
    private static final UUID ALIASED_AUTHOR = id(2);
    private static final UUID AUTHOR_LABEL = id(3);
    private static final UUID DEVELOPER_GROUP = id(4);
    private static final UUID ALIASED_GROUP = id(5);
    private static final UUID GROUP_LABEL = id(6);
    private static final UUID UNKNOWN = id(99);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AccessTokens accessTokens;

    @BeforeEach
    void insertPeopleGroupsAndAliases() {
        TestActors.store(jdbc);
        jdbc.update(
                "INSERT INTO person (id, real_name) VALUES (?, 'Jane Publisher'), (?, 'John Publisher')",
                AUTHOR,
                ALIASED_AUTHOR);
        jdbc.update(
                "INSERT INTO \"group\" (id, name) VALUES (?, 'Nebula Studio'), (?, 'Solar Studio')",
                DEVELOPER_GROUP,
                ALIASED_GROUP);
        jdbc.update("INSERT INTO label (id, name, person_id) VALUES (?, 'J.P.', ?)", AUTHOR_LABEL, ALIASED_AUTHOR);
        jdbc.update("INSERT INTO label (id, name, group_id) VALUES (?, 'Solar', ?)", GROUP_LABEL, ALIASED_GROUP);
    }

    @Test
    void creditsPeopleGroupsAndTheirLabelsUnderTheChosenNamesWithTheirRoles() throws Exception {
        String created = mvc.perform(post("/api/software")
                        .with(TestActors.bearer(accessTokens, MEMBER))
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Elite",
                                  "platform": "vector",
                                  "credits": [
                                    {"type": "author", "id": "%s", "roles": ["publisher"]},
                                    {"type": "author-label", "id": "%s", "roles": ["code"]},
                                    {"type": "group", "id": "%s", "roles": ["developer"]},
                                    {"type": "group-label", "id": "%s", "roles": ["developer", "publisher"]}
                                  ]
                                }
                                """.formatted(AUTHOR, AUTHOR_LABEL, DEVELOPER_GROUP, GROUP_LABEL)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String softwareId = JsonPath.read(created, "$.id");
        entityManager.flush();

        mvc.perform(get("/api/software/{id}", softwareId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishers").doesNotExist())
                .andExpect(jsonPath("$.developers").doesNotExist())
                .andExpect(jsonPath("$.credits[*].type")
                        .value(containsInAnyOrder("author", "author-label", "group", "group-label")))
                .andExpect(jsonPath("$.credits[*].title").isNotEmpty())
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(AUTHOR))
                        .value(contains("publisher")))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].ownerId".formatted(AUTHOR_LABEL))
                        .value(contains(ALIASED_AUTHOR.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].ownerId".formatted(DEVELOPER_GROUP))
                        .value(contains(DEVELOPER_GROUP.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(DEVELOPER_GROUP))
                        .value(contains("developer")))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].ownerId".formatted(GROUP_LABEL))
                        .value(contains(ALIASED_GROUP.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(GROUP_LABEL))
                        .value(contains("developer", "publisher")));
    }

    @Test
    void findsPeopleGroupsAndTheirAliasesForTheForm() throws Exception {
        mvc.perform(get("/api/entity-lookup")
                        .param("types", "author,author-label,group,group-label")
                        .param("query", "publisher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id")
                        .value(hasItems(AUTHOR.toString(), ALIASED_AUTHOR.toString(), AUTHOR_LABEL.toString())));

        mvc.perform(get("/api/entity-lookup")
                        .param("types", "group,group-label")
                        .param("query", "solar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id").value(hasItems(ALIASED_GROUP.toString(), GROUP_LABEL.toString())))
                .andExpect(jsonPath("$.items[*].type").value(hasItems("group", "group-label")));
    }

    @Test
    void offersALabelOfNobodyAsALabelOfAPersonWithoutAnOwner() throws Exception {
        UUID ghost = id(7);
        jdbc.update("INSERT INTO label (id, name) VALUES (?, 'Ghost Publisher')", ghost);

        mvc.perform(get("/api/entity-lookup")
                        .param("types", "author,author-label")
                        .param("query", "ghost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(ghost.toString()))
                .andExpect(jsonPath("$.items[0].type").value("author-label"))
                .andExpect(jsonPath("$.items[0].ownerId").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void refusesACreditedGroupThatIsNotInTheArchive() throws Exception {
        mvc.perform(post("/api/software")
                        .with(TestActors.bearer(accessTokens, MEMBER))
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Elite",
                                  "platform": "vector",
                                  "credits": [{"type": "group", "id": "%s", "roles": ["developer"]}]
                                }
                                """.formatted(UNKNOWN)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("software.credit-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("credits"));
    }

    @Test
    void refusesAGroupCreditedTwice() throws Exception {
        mvc.perform(post("/api/software")
                        .with(TestActors.bearer(accessTokens, MEMBER))
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Elite",
                                  "platform": "vector",
                                  "credits": [
                                    {"type": "group", "id": "%1$s", "roles": ["developer"]},
                                    {"type": "group", "id": "%1$s", "roles": ["publisher"]}
                                  ]
                                }
                                """.formatted(DEVELOPER_GROUP)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("software.credit-duplicate"));
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00aa-%012d".formatted(number));
    }
}
