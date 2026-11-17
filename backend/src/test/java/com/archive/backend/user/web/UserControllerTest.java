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
import com.archive.backend.user.app.EditUserCommand;
import com.archive.backend.user.app.EditUserHandler;
import com.archive.backend.user.app.GetUserHandler;
import com.archive.backend.user.app.GetUserQuery;
import com.archive.backend.user.app.ListUsersHandler;
import com.archive.backend.user.app.ListUsersQuery;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.GrantedGroups;
import com.archive.backend.user.domain.LastAdministratorException;
import com.archive.backend.user.domain.SignInRequiredException;
import com.archive.backend.user.domain.UnconfirmedUserGroupsException;
import com.archive.backend.user.domain.UserEmailTakenException;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserName;
import com.archive.backend.user.domain.UserNameTakenException;
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
    private static final String EDIT_ZED_BODY =
            "{\"name\":\"Zed\",\"email\":\"zed@example.com\",\"groups\":[\"moderator\"]}";
    private static final EditUserCommand EDIT_ZED = new EditUserCommand(
            Actor.anonymous(),
            new UserId(ZED),
            new UserName("Zed"),
            new Email("zed@example.com"),
            new GrantedGroups(Set.of(UserGroup.MODERATOR)));

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListUsersHandler listHandler;

    @MockitoBean
    private GetUserHandler getHandler;

    @MockitoBean
    private EditUserHandler editHandler;

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
    void readsAUser() throws Exception {
        when(getHandler.handle(new GetUserQuery(Actor.anonymous(), new UserId(ZED))))
                .thenReturn(new UserEntry(ZED, "Zed", "zed@example.com", true, REGISTERED, List.of("admin", "member")));

        mvc.perform(get("/api/users/{id}", ZED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ZED.toString()))
                .andExpect(jsonPath("$.name").value("Zed"))
                .andExpect(jsonPath("$.email").value("zed@example.com"))
                .andExpect(jsonPath("$.confirmed").value(true))
                .andExpect(jsonPath("$.groups[0]").value("admin"))
                .andExpect(jsonPath("$.groups[1]").value("member"));
    }

    @Test
    void tellsOfAnUnknownUserToRead() throws Exception {
        when(getHandler.handle(any())).thenThrow(new UserNotFoundException());

        mvc.perform(get("/api/users/{id}", ZED))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("user.not-found"));
    }

    @Test
    void tellsAPublicVisitorToSignInToReadAUser() throws Exception {
        when(getHandler.handle(any())).thenThrow(new SignInRequiredException());

        mvc.perform(get("/api/users/{id}", ZED))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.required"));
    }

    @Test
    void refusesAUserToANonAdministrator() throws Exception {
        when(getHandler.handle(any())).thenThrow(new ChangeForbiddenException());

        mvc.perform(get("/api/users/{id}", ZED))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("access.denied"));
    }

    @Test
    void editsAUser() throws Exception {
        mvc.perform(edit(ZED, EDIT_ZED_BODY)).andExpect(status().isNoContent());

        verify(editHandler).handle(EDIT_ZED);
    }

    @Test
    void takesAwayEveryGroupGivenOnPurpose() throws Exception {
        mvc.perform(edit(ZED, "{\"name\":\"Axe\",\"email\":\"axe@example.com\",\"groups\":[]}"))
                .andExpect(status().isNoContent());

        verify(editHandler)
                .handle(new EditUserCommand(
                        Actor.anonymous(),
                        new UserId(ZED),
                        new UserName("Axe"),
                        new Email("axe@example.com"),
                        new GrantedGroups(Set.of())));
    }

    @Test
    void refusesAMissingName() throws Exception {
        mvc.perform(edit(ZED, "{\"email\":\"axe@example.com\",\"groups\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("user.name-required"));

        verify(editHandler, never()).handle(any());
    }

    @Test
    void refusesABlankName() throws Exception {
        mvc.perform(edit(ZED, "{\"name\":\" \",\"email\":\"axe@example.com\",\"groups\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("user.name-required"));
    }

    @Test
    void refusesAMissingEmail() throws Exception {
        mvc.perform(edit(ZED, "{\"name\":\"Axe\",\"groups\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("user.email-required"));
    }

    @Test
    void refusesAnEmailThatIsNotOne() throws Exception {
        mvc.perform(edit(ZED, "{\"name\":\"Axe\",\"email\":\"axe\",\"groups\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("user.email-invalid"));
    }

    @Test
    void refusesAGroupNotGivenOnPurpose() throws Exception {
        mvc.perform(edit(ZED, "{\"name\":\"Axe\",\"email\":\"axe@example.com\",\"groups\":[\"member\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("user.group-invalid"));

        verify(editHandler, never()).handle(any());
    }

    @Test
    void refusesAnUnknownGroup() throws Exception {
        mvc.perform(edit(ZED, "{\"name\":\"Axe\",\"email\":\"axe@example.com\",\"groups\":[\"owner\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("user.group-invalid"));
    }

    @Test
    void refusesARequestWithoutGroups() throws Exception {
        mvc.perform(edit(ZED, "{\"name\":\"Axe\",\"email\":\"axe@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("user.groups-required"));

        verify(editHandler, never()).handle(any());
    }

    @Test
    void refusesAMalformedUserId() throws Exception {
        mvc.perform(put("/api/users/not-an-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EDIT_ZED_BODY))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tellsOfAnUnknownUserToEdit() throws Exception {
        doThrow(new UserNotFoundException()).when(editHandler).handle(EDIT_ZED);

        mvc.perform(edit(ZED, EDIT_ZED_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("user.not-found"));
    }

    @Test
    void refusesATakenName() throws Exception {
        doThrow(new UserNameTakenException()).when(editHandler).handle(EDIT_ZED);

        mvc.perform(edit(ZED, EDIT_ZED_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("user.name-taken"))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void refusesATakenEmail() throws Exception {
        doThrow(new UserEmailTakenException()).when(editHandler).handle(EDIT_ZED);

        mvc.perform(edit(ZED, EDIT_ZED_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("user.email-taken"))
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @Test
    void keepsTheLastAdministrator() throws Exception {
        doThrow(new LastAdministratorException()).when(editHandler).handle(EDIT_ZED);

        mvc.perform(edit(ZED, EDIT_ZED_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("user.last-admin"));
    }

    @Test
    void givesNoGroupsToAnUnconfirmedAccount() throws Exception {
        doThrow(new UnconfirmedUserGroupsException()).when(editHandler).handle(EDIT_ZED);

        mvc.perform(edit(ZED, EDIT_ZED_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("user.unconfirmed-groups"));
    }

    @Test
    void tellsAPublicVisitorToSignInToEdit() throws Exception {
        doThrow(new SignInRequiredException()).when(editHandler).handle(EDIT_ZED);

        mvc.perform(edit(ZED, EDIT_ZED_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.required"));
    }

    @Test
    void refusesTheEditToANonAdministrator() throws Exception {
        doThrow(new ChangeForbiddenException()).when(editHandler).handle(EDIT_ZED);

        mvc.perform(edit(ZED, EDIT_ZED_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("access.denied"));
    }

    private static RequestBuilder edit(UUID id, String body) {
        return put("/api/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }
}
