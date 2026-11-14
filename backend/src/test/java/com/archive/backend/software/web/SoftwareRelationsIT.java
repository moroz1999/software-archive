package com.archive.backend.software.web;

import static com.archive.backend.user.TestActors.MEMBER;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.session.AccessTokens;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
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
import org.springframework.transaction.annotation.Transactional;

/** Publishers and developer companies chosen while software is added or edited. */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class})
@Transactional
@Rollback
class SoftwareRelationsIT {

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
    void storesPublishersAndDeveloperCompaniesUnderTheChosenNames() throws Exception {
        String created = mvc.perform(post("/api/software")
                        .cookie(TestActors.accessCookie(accessTokens, MEMBER))
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Elite",
                                  "platform": "vector",
                                  "publishers": [
                                    {"type": "author", "id": "%s"},
                                    {"type": "author-label", "id": "%s"},
                                    {"type": "group-label", "id": "%s"}
                                  ],
                                  "developers": [
                                    {"type": "group", "id": "%s"},
                                    {"type": "group-label", "id": "%s"}
                                  ]
                                }
                                """.formatted(AUTHOR, AUTHOR_LABEL, GROUP_LABEL, DEVELOPER_GROUP, GROUP_LABEL)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String softwareId = JsonPath.read(created, "$.id");
        entityManager.flush();

        mvc.perform(get("/api/software/{id}", softwareId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishers[*].id")
                        .value(containsInAnyOrder(AUTHOR.toString(), AUTHOR_LABEL.toString(), GROUP_LABEL.toString())))
                .andExpect(jsonPath("$.publishers[*].type")
                        .value(containsInAnyOrder("author", "author-label", "group-label")))
                .andExpect(jsonPath("$.developers[*].id")
                        .value(containsInAnyOrder(DEVELOPER_GROUP.toString(), GROUP_LABEL.toString())))
                .andExpect(jsonPath("$.developers[*].type").value(containsInAnyOrder("group", "group-label")))
                .andExpect(jsonPath("$.publishers[*].title").isNotEmpty())
                .andExpect(jsonPath("$.developers[*].title").isNotEmpty())
                .andExpect(jsonPath("$.publishers[?(@.id == '%s')].ownerId".formatted(AUTHOR_LABEL))
                        .value(contains(ALIASED_AUTHOR.toString())))
                .andExpect(jsonPath("$.publishers[?(@.id == '%s')].ownerId".formatted(GROUP_LABEL))
                        .value(contains(ALIASED_GROUP.toString())))
                .andExpect(jsonPath("$.developers[?(@.id == '%s')].ownerId".formatted(GROUP_LABEL))
                        .value(contains(ALIASED_GROUP.toString())));
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
    void refusesAPublisherThatIsNotInTheArchive() throws Exception {
        mvc.perform(post("/api/software")
                        .cookie(TestActors.accessCookie(accessTokens, MEMBER))
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Elite",
                                  "platform": "vector",
                                  "publishers": [{"type": "author", "id": "%s"}]
                                }
                                """.formatted(UNKNOWN)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("software.publisher-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("publishers"));
    }

    @Test
    void refusesAnAuthorAsADeveloperCompany() throws Exception {
        mvc.perform(post("/api/software")
                        .cookie(TestActors.accessCookie(accessTokens, MEMBER))
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Elite",
                                  "platform": "vector",
                                  "developers": [{"type": "author", "id": "%s"}]
                                }
                                """.formatted(AUTHOR)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("software.reference-type-invalid"));
    }

    @Test
    void refusesADeveloperCompanyThatIsNotInTheArchive() throws Exception {
        mvc.perform(post("/api/software")
                        .cookie(TestActors.accessCookie(accessTokens, MEMBER))
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Elite",
                                  "platform": "vector",
                                  "developers": [{"type": "group", "id": "%s"}]
                                }
                                """.formatted(UNKNOWN)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("software.developer-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("developers"));
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00aa-%012d".formatted(number));
    }
}
