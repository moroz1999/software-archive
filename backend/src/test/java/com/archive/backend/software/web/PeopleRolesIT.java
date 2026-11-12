package com.archive.backend.software.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
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

/** Roles held by people, under their own name or an alias, in groups and software. */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class})
@Transactional
@Rollback
class PeopleRolesIT {

    private static final UUID PERSON = id(1);
    private static final UUID ALIAS = id(2);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void insertPersonAndAlias() {
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Jane Doe')", PERSON);
        jdbc.update("INSERT INTO label (id, name, person_id) VALUES (?, 'J.D.', ?)", ALIAS, PERSON);
    }

    @Test
    void storesGroupMemberRolesAndDatesUnderTheChosenAlias() throws Exception {
        String created = mvc.perform(
                        post("/api/groups").contentType("application/json").content("""
                                {
                                  "name": "Crew",
                                  "types": ["demogroup"],
                                  "members": [{
                                    "id": "%s",
                                    "type": "author-label",
                                    "roles": ["coder", "organizer"],
                                    "startDate": "1990-01-01",
                                    "endDate": "1995-12-31"
                                  }]
                                }
                                """.formatted(ALIAS)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String groupId = JsonPath.read(created, "$.id");
        entityManager.flush();

        mvc.perform(get("/api/groups/{id}", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].id").value(ALIAS.toString()))
                .andExpect(jsonPath("$.members[0].type").value("author-label"))
                .andExpect(jsonPath("$.members[0].roles[0]").value("coder"))
                .andExpect(jsonPath("$.members[0].roles[1]").value("organizer"))
                .andExpect(jsonPath("$.members[0].startDate").value("1990-01-01"))
                .andExpect(jsonPath("$.members[0].endDate").value("1995-12-31"));
    }

    @Test
    void storesSeveralSoftwareRolesForAPerson() throws Exception {
        String created = mvc.perform(
                        post("/api/software").contentType("application/json").content("""
                                {
                                  "title": "Elite",
                                  "platform": "vector",
                                  "credits": [{
                                    "id": "%s",
                                    "type": "author",
                                    "roles": ["code", "gamedesign"]
                                  }]
                                }
                                """.formatted(PERSON)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String softwareId = JsonPath.read(created, "$.id");
        entityManager.flush();

        mvc.perform(get("/api/software/{id}", softwareId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credits[0].id").value(PERSON.toString()))
                .andExpect(jsonPath("$.credits[0].type").value("author"))
                .andExpect(jsonPath("$.credits[0].roles[0]").value("code"))
                .andExpect(jsonPath("$.credits[0].roles[1]").value("gamedesign"));
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00bb-%012d".formatted(number));
    }
}
