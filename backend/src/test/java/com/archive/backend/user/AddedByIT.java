package com.archive.backend.user;

import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.archive.backend.user.domain.session.AccessTokens;
import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

/**
 * Every entry shows on its page who added it and when (docs/domain/users.md#user-groups): a person, a group, a label,
 * software and a release.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class})
@Transactional
@Rollback
class AddedByIT {

    private static final UUID JANE = id(1);
    private static final UUID CREW = id(2);
    private static final UUID GHOST = id(3);
    private static final UUID ELITE = id(4);
    private static final String WHEN = "2026-03-04T05:06:07Z";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccessTokens accessTokens;

    /** Each entry added by the member at the same moment. */
    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        UUID member = MEMBER_ID.value();
        jdbc.update(
                "INSERT INTO person (id, real_name, added_by, created_at) VALUES (?, 'Jane Doe', ?, ?::timestamptz)",
                JANE,
                member,
                WHEN);
        jdbc.update(
                "INSERT INTO \"group\" (id, name, added_by, created_at) VALUES (?, 'Crew', ?, ?::timestamptz)",
                CREW,
                member,
                WHEN);
        jdbc.update(
                "INSERT INTO label (id, name, is_display_name, added_by, created_at) VALUES (?, 'Ghost', false, ?, ?::timestamptz)",
                GHOST,
                member,
                WHEN);
        jdbc.update(
                "INSERT INTO software (id, title, platform_code, added_by, created_at) VALUES (?, 'Elite', 'vector', ?, ?::timestamptz)",
                ELITE,
                member,
                WHEN);
    }

    @Test
    void showsWhoAddedAPersonAndWhen() throws Exception {
        mvc.perform(get("/api/authors/{id}", JANE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.added.by").value("TestMember"))
                .andExpect(jsonPath("$.added.at").value(WHEN));
    }

    @Test
    void showsWhoAddedAGroupAndWhen() throws Exception {
        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.added.by").value("TestMember"))
                .andExpect(jsonPath("$.added.at").value(WHEN));
    }

    @Test
    void showsWhoAddedALabelAndWhen() throws Exception {
        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.added.by").value("TestMember"))
                .andExpect(jsonPath("$.added.at").value(WHEN));
    }

    @Test
    void showsWhoAddedSoftwareAndWhen() throws Exception {
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.added.by").value("TestMember"))
                .andExpect(jsonPath("$.added.at").value(WHEN));
    }

    @Test
    void remembersWhoAddedAReleaseAndWhen() throws Exception {
        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        mvc.perform(post("/api/software/{id}/releases", ELITE)
                        .with(TestActors.bearer(accessTokens, MEMBER))
                        .contentType("application/json")
                        .content("{\"type\": \"original\", \"hardware\": []}"))
                .andExpect(status().isCreated());

        ResultActions releases = mvc.perform(get("/api/software/{id}/releases", ELITE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].added.by").value("TestMember"));
        String at = JsonPath.read(releases.andReturn().getResponse().getContentAsString(), "$[0].added.at");
        assertThat(Instant.parse(at)).isAfterOrEqualTo(before);
    }

    @Test
    void showsOnlyWhenAnEntryWasAddedOnceTheAccountOfWhoeverAddedItIsGone() throws Exception {
        jdbc.update(
                "INSERT INTO software_release (id, software_id, type, added_by) VALUES (?, ?, 'original', ?)",
                id(5),
                ELITE,
                MEMBER_ID.value());
        jdbc.update("DELETE FROM app_user WHERE id = ?", MEMBER_ID.value());

        mvc.perform(get("/api/software/{id}/releases", ELITE))
                .andExpect(jsonPath("$[0].added.by").value(nullValue()))
                .andExpect(jsonPath("$[0].added.at").isNotEmpty());

        mvc.perform(get("/api/authors/{id}", JANE))
                .andExpect(jsonPath("$.added.by").value(nullValue()))
                .andExpect(jsonPath("$.added.at").value(WHEN));
        mvc.perform(get("/api/groups/{id}", CREW))
                .andExpect(jsonPath("$.added.by").value(nullValue()))
                .andExpect(jsonPath("$.added.at").value(WHEN));
        mvc.perform(get("/api/labels/{id}", GHOST))
                .andExpect(jsonPath("$.added.by").value(nullValue()))
                .andExpect(jsonPath("$.added.at").value(WHEN));
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.added.by").value(nullValue()))
                .andExpect(jsonPath("$.added.at").value(WHEN));
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00ad-%012d".formatted(number));
    }
}
