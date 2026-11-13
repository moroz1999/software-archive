package com.archive.backend.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.archive.backend.user.domain.ConfirmationMailer;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.InterfaceLanguage;
import com.archive.backend.user.domain.UserName;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * An account from registration to signing out, through the real application and database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class, AuthFlowIT.Letters.class})
class AuthFlowIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Letters letters;

    @Test
    void anAccountFromRegistrationToSigningOut() throws Exception {
        register("FlowUser", "flow@example.com");
        ConfirmationToken token = letters.tokenOf("flow@example.com");

        mvc.perform(login("FlowUser"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("user.unconfirmed"));

        MvcResult confirmed = mvc.perform(post("/api/auth/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"" + token.value() + "\"}"))
                .andExpect(status().isNoContent())
                .andReturn();
        Cookie access = cookie(confirmed, "access_token");
        Cookie refresh = cookie(confirmed, "refresh_token");

        mvc.perform(get("/api/auth/me").cookie(access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("FlowUser"))
                .andExpect(jsonPath("$.email").value("flow@example.com"))
                .andExpect(jsonPath("$.groups").isArray());

        MvcResult refreshed = mvc.perform(post("/api/auth/refresh").cookie(refresh))
                .andExpect(status().isNoContent())
                .andReturn();
        Cookie newRefresh = cookie(refreshed, "refresh_token");
        mvc.perform(get("/api/auth/me").cookie(cookie(refreshed, "access_token")))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/refresh").cookie(refresh))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.session-expired"));

        mvc.perform(post("/api/auth/logout").cookie(newRefresh)).andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/refresh").cookie(newRefresh)).andExpect(status().isUnauthorized());
    }

    @Test
    void aLaterAccountSignsInByEmailAsAMember() throws Exception {
        register("FirstUser", "first@example.com");
        confirm("first@example.com");
        register("LaterUser", "later@example.com");
        confirm("later@example.com");

        MvcResult signedIn = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"LATER@example.com\", \"password\": \"password1\"}"))
                .andExpect(status().isNoContent())
                .andReturn();

        mvc.perform(get("/api/auth/me").cookie(cookie(signedIn, "access_token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups", contains("member")));
    }

    @Test
    void aPublicVisitorIsNobody() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isNoContent());
    }

    private void register(String name, String email) throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"name\": \"%s\", \"email\": \"%s\", \"password\": \"password1\", \"language\": \"en\"}"
                                        .formatted(name, email)))
                .andExpect(status().isNoContent());
    }

    private void confirm(String email) throws Exception {
        mvc.perform(post("/api/auth/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"" + letters.tokenOf(email).value() + "\"}"))
                .andExpect(status().isNoContent());
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder login(String login) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\": \"" + login + "\", \"password\": \"password1\"}");
    }

    private static Cookie cookie(MvcResult result, String name) {
        Cookie cookie = result.getResponse().getCookie(name);
        assertThat(cookie).as(name).isNotNull();
        return Objects.requireNonNull(cookie);
    }

    /** Keeps the letters instead of mailing them. */
    @TestConfiguration
    static class Letters {

        private final Map<String, ConfirmationToken> tokens = new ConcurrentHashMap<>();

        @Bean
        @Primary
        ConfirmationMailer recordingMailer() {
            return (Email to, UserName name, ConfirmationToken token, InterfaceLanguage language) ->
                    tokens.put(to.value(), token);
        }

        ConfirmationToken tokenOf(String email) {
            return Objects.requireNonNull(tokens.get(email), email);
        }
    }
}
