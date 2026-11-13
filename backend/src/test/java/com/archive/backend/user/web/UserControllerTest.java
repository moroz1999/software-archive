package com.archive.backend.user.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import com.archive.backend.user.app.ChangeUserGroupsCommand;
import com.archive.backend.user.app.ChangeUserGroupsHandler;
import com.archive.backend.user.app.ListUsersHandler;
import com.archive.backend.user.app.ListUsersQuery;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.GrantedGroups;
import com.archive.backend.user.domain.LastAdministratorException;
import com.archive.backend.user.domain.SignInRequiredException;
import com.archive.backend.user.domain.UnconfirmedUserGroupsException;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserNotFoundException;
import com.archive.backend.user.domain.UserSearch;
import com.archive.backend.user.read.UserEntry;
import com.archive.backend.user.web.mapper.UserManagementWebMapperImpl;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, WebConfig.class, UserManagementWebMapperImpl.class, ApiExceptionHandler.class})
class UserControllerTest {

    private static final UUID ZED = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID AXE = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final Instant REGISTERED = Instant.parse("2026-01-01T00:00:00Z");
    private static final ChangeUserGroupsCommand MAKE_ZED_A_MODERATOR = new ChangeUserGroupsCommand(
            Actor.anonymous(), new UserId(ZED), new GrantedGroups(Set.of(UserGroup.MODERATOR)));

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListUsersHandler listHandler;

    @MockitoBean
    private ChangeUserGroupsHandler changeGroupsHandler;

    @Test
    void listsAPageOfUsers() throws Exception {
        when(listHandler.handle(new ListUsersQuery(Actor.anonymous(), null, PageRequest.of(0, 50))))
                .thenReturn(new PageImpl<>(
                        List.of(
                                new UserEntry(
                                        ZED, "Zed", "zed@example.com", true, REGISTERED, List.of("admin", "member")),
                                new UserEntry(AXE, "Axe", "axe@example.com", false, REGISTERED, List.of())),
                        PageRequest.of(0, 50),
                        2));

        mvc.perform(get("/api/users").param("page", "0").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(ZED.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Zed"))
                .andExpect(jsonPath("$.content[0].email").value("zed@example.com"))
                .andExpect(jsonPath("$.content[0].confirmed").value(true))
                .andExpect(jsonPath("$.content[0].registeredAt").value("2026-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.content[0].groups[0]").value("admin"))
                .andExpect(jsonPath("$.content[0].groups[1]").value("member"))
                .andExpect(jsonPath("$.content[1].confirmed").value(false))
                .andExpect(jsonPath("$.content[1].groups").isEmpty())
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.size").value(50));
    }

    @Test
    void listsTheUsersOfASearch() throws Exception {
        when(listHandler.handle(new ListUsersQuery(Actor.anonymous(), new UserSearch("ze"), PageRequest.of(1, 50))))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 50), 50));

        mvc.perform(get("/api/users").param("query", " ze ").param("page", "1").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.number").value(1));
    }

    @Test
    void tellsAPublicVisitorToSignInToListUsers() throws Exception {
        when(listHandler.handle(any())).thenThrow(new SignInRequiredException());

        mvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.required"));
    }

    @Test
    void refusesTheListToANonAdministrator() throws Exception {
        when(listHandler.handle(any())).thenThrow(new ChangeForbiddenException());

        mvc.perform(get("/api/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("access.denied"));
    }

    @Test
    void changesTheGroupsOfAUser() throws Exception {
        mvc.perform(groups(ZED, "{\"groups\":[\"moderator\"]}")).andExpect(status().isNoContent());

        verify(changeGroupsHandler).handle(MAKE_ZED_A_MODERATOR);
    }

    @Test
    void takesAwayEveryGroupGivenOnPurpose() throws Exception {
        mvc.perform(groups(ZED, "{\"groups\":[]}")).andExpect(status().isNoContent());

        verify(changeGroupsHandler)
                .handle(new ChangeUserGroupsCommand(Actor.anonymous(), new UserId(ZED), new GrantedGroups(Set.of())));
    }

    @Test
    void refusesAGroupNotGivenOnPurpose() throws Exception {
        mvc.perform(groups(ZED, "{\"groups\":[\"member\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("user.group-invalid"));

        verify(changeGroupsHandler, never()).handle(any());
    }

    @Test
    void refusesAnUnknownGroup() throws Exception {
        mvc.perform(groups(ZED, "{\"groups\":[\"owner\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("user.group-invalid"));
    }

    @Test
    void refusesARequestWithoutGroups() throws Exception {
        mvc.perform(groups(ZED, "{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("user.groups-required"));

        verify(changeGroupsHandler, never()).handle(any());
    }

    @Test
    void refusesAMalformedUserId() throws Exception {
        mvc.perform(put("/api/users/not-an-id/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groups\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tellsOfAnUnknownUser() throws Exception {
        doThrow(new UserNotFoundException()).when(changeGroupsHandler).handle(MAKE_ZED_A_MODERATOR);

        mvc.perform(groups(ZED, "{\"groups\":[\"moderator\"]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("user.not-found"));
    }

    @Test
    void keepsTheLastAdministrator() throws Exception {
        doThrow(new LastAdministratorException()).when(changeGroupsHandler).handle(MAKE_ZED_A_MODERATOR);

        mvc.perform(groups(ZED, "{\"groups\":[\"moderator\"]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("user.last-admin"));
    }

    @Test
    void givesNoGroupsToAnUnconfirmedAccount() throws Exception {
        doThrow(new UnconfirmedUserGroupsException()).when(changeGroupsHandler).handle(MAKE_ZED_A_MODERATOR);

        mvc.perform(groups(ZED, "{\"groups\":[\"moderator\"]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("user.unconfirmed-groups"));
    }

    @Test
    void tellsAPublicVisitorToSignInToChangeGroups() throws Exception {
        doThrow(new SignInRequiredException()).when(changeGroupsHandler).handle(MAKE_ZED_A_MODERATOR);

        mvc.perform(groups(ZED, "{\"groups\":[\"moderator\"]}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.required"));
    }

    @Test
    void refusesTheChangeToANonAdministrator() throws Exception {
        doThrow(new ChangeForbiddenException()).when(changeGroupsHandler).handle(MAKE_ZED_A_MODERATOR);

        mvc.perform(groups(ZED, "{\"groups\":[\"moderator\"]}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("access.denied"));
    }

    private static RequestBuilder groups(UUID id, String body) {
        return put("/api/users/{id}/groups", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }
}
