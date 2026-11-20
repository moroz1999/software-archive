package com.archive.backend.fixtures;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * The fixture accounts: one of every group, all with the password {@code password1}.
 */
@IntegrationTest
@ActiveProfiles("fixtures")
class UserFixturesIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void everyGroupHasAFixtureAccount() throws Exception {
        assertGroups("admin", List.of("admin", "member"));
        assertGroups("moderator@archive.test", List.of("member", "moderator"));
        assertGroups("member", List.of("member"));
    }

    @Test
    void aFixtureAccountWaitsForItsLink() throws Exception {
        logIn("pending")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("user.unconfirmed"));
    }

    private void assertGroups(String login, List<String> groups) throws Exception {
        String access = JsonPath.read(
                logIn(login)
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                "$.accessToken");

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups", contains(groups.toArray())));
    }

    private ResultActions logIn(String login) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\": \"" + login + "\", \"password\": \"password1\"}"));
    }
}
