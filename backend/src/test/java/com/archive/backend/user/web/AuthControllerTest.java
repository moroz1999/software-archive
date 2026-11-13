package com.archive.backend.user.web;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.shared.config.AuthProperties;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.domain.error.AuthenticationRequiredException;
import com.archive.backend.shared.web.ApiExceptionHandler;
import com.archive.backend.user.app.ConfirmEmailCommand;
import com.archive.backend.user.app.ConfirmEmailHandler;
import com.archive.backend.user.app.GetCurrentUserHandler;
import com.archive.backend.user.app.GetCurrentUserQuery;
import com.archive.backend.user.app.LogInCommand;
import com.archive.backend.user.app.LogInHandler;
import com.archive.backend.user.app.LogOutCommand;
import com.archive.backend.user.app.LogOutHandler;
import com.archive.backend.user.app.RefreshSessionCommand;
import com.archive.backend.user.app.RefreshSessionHandler;
import com.archive.backend.user.app.RegisterUserCommand;
import com.archive.backend.user.app.RegisterUserHandler;
import com.archive.backend.user.app.ResendConfirmationCommand;
import com.archive.backend.user.app.ResendConfirmationHandler;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ConfirmationExpiredException;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.CredentialsInvalidException;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.InterfaceLanguage;
import com.archive.backend.user.domain.Login;
import com.archive.backend.user.domain.Password;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserName;
import com.archive.backend.user.domain.UserNameTakenException;
import com.archive.backend.user.domain.UserUnconfirmedException;
import com.archive.backend.user.domain.session.AccessToken;
import com.archive.backend.user.domain.session.IssuedSession;
import com.archive.backend.user.domain.session.RefreshSecret;
import com.archive.backend.user.domain.session.RememberMe;
import com.archive.backend.user.domain.session.SessionExpiredException;
import com.archive.backend.user.read.CurrentUser;
import com.archive.backend.user.web.mapper.AuthWebMapperImpl;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, WebConfig.class, AuthWebMapperImpl.class, ApiExceptionHandler.class, SessionCookies.class
})
class AuthControllerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final IssuedSession REMEMBERED = new IssuedSession(
            new AccessToken("access-jwt", Instant.parse("2026-01-01T00:15:00Z")),
            new RefreshSecret("refresh-secret"),
            new RememberMe(true),
            Instant.parse("2026-01-31T00:00:00Z"));
    private static final IssuedSession FORGOTTEN =
            new IssuedSession(REMEMBERED.access(), REMEMBERED.refresh(), new RememberMe(false), REMEMBERED.expiresAt());

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AuthProperties properties;

    @MockitoBean
    private RegisterUserHandler registerHandler;

    @MockitoBean
    private ConfirmEmailHandler confirmHandler;

    @MockitoBean
    private ResendConfirmationHandler resendHandler;

    @MockitoBean
    private LogInHandler logInHandler;

    @MockitoBean
    private RefreshSessionHandler refreshHandler;

    @MockitoBean
    private LogOutHandler logOutHandler;

    @MockitoBean
    private GetCurrentUserHandler currentUserHandler;

    @Test
    void registersAnAccount() throws Exception {
        mvc.perform(json(post("/api/auth/register"), """
                        {"name": "Zed", "email": "Zed@Example.com", "password": "password1", "language": "ru"}""")).andExpect(status().isNoContent());

        verify(registerHandler)
                .handle(new RegisterUserCommand(
                        new UserName("Zed"),
                        new Email("zed@example.com"),
                        new Password("password1"),
                        InterfaceLanguage.RU));
    }

    @Test
    void aRegistrationWithoutALanguageGetsAnEnglishLetter() throws Exception {
        mvc.perform(json(post("/api/auth/register"), """
                        {"name": "Zed", "email": "zed@example.com", "password": "password1"}""")).andExpect(status().isNoContent());

        verify(registerHandler)
                .handle(new RegisterUserCommand(
                        new UserName("Zed"),
                        new Email("zed@example.com"),
                        new Password("password1"),
                        InterfaceLanguage.EN));
    }

    @Test
    void answers422ForATakenUserName() throws Exception {
        doThrow(new UserNameTakenException()).when(registerHandler).handle(any());

        mvc.perform(json(post("/api/auth/register"), """
                        {"name": "Zed", "email": "zed@example.com", "password": "password1"}"""))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("user.name-taken"))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void answers400ForAMalformedEmail() throws Exception {
        mvc.perform(json(post("/api/auth/register"), """
                        {"name": "Zed", "email": "zed", "password": "password1"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("user.email-invalid"));
        verify(registerHandler, never()).handle(any());
    }

    @Test
    void answers400ForAMissingName() throws Exception {
        mvc.perform(json(post("/api/auth/register"), """
                        {"email": "zed@example.com", "password": "password1"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("user.name-required"));
    }

    @Test
    void confirmingSignsInWithHttpOnlyCookies() throws Exception {
        when(confirmHandler.handle(new ConfirmEmailCommand(new ConfirmationToken("token"))))
                .thenReturn(REMEMBERED);

        mvc.perform(json(post("/api/auth/confirm"), "{\"token\": \"token\"}"))
                .andExpect(status().isNoContent())
                .andExpect(header().stringValues(
                                HttpHeaders.SET_COOKIE,
                                hasItem(allOf(
                                        containsString("access_token=access-jwt"),
                                        containsString("Path=/api"),
                                        containsString("Max-Age=900"),
                                        containsString("HttpOnly"),
                                        containsString("Secure"),
                                        containsString("SameSite=Strict")))))
                .andExpect(header().stringValues(
                                HttpHeaders.SET_COOKIE,
                                hasItem(allOf(
                                        containsString("refresh_token=refresh-secret"),
                                        containsString("Path=/api/auth"),
                                        containsString("Max-Age=2592000"),
                                        containsString("HttpOnly"),
                                        containsString("Secure"),
                                        containsString("SameSite=Strict")))));
    }

    @Test
    void answers422ForAnExpiredLink() throws Exception {
        when(confirmHandler.handle(any())).thenThrow(new ConfirmationExpiredException());

        mvc.perform(json(post("/api/auth/confirm"), "{\"token\": \"token\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("user.confirmation-expired"))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    void resendsTheLink() throws Exception {
        mvc.perform(json(post("/api/auth/resend-confirmation"), """
                        {"login": "Zed", "password": "password1", "language": "ru"}""")).andExpect(status().isNoContent());

        verify(resendHandler)
                .handle(new ResendConfirmationCommand(
                        new Login("Zed"), new Password("password1"), InterfaceLanguage.RU));
    }

    @Test
    void signsIn() throws Exception {
        when(logInHandler.handle(new LogInCommand(new Login("Zed"), new Password("password1"), new RememberMe(true))))
                .thenReturn(REMEMBERED);

        mvc.perform(json(post("/api/auth/login"), """
                        {"login": "Zed", "password": "password1", "remember": true}"""))
                .andExpect(status().isNoContent())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(containsString("Max-Age=2592000"))));
    }

    @Test
    void aSessionNotRememberedEndsWithTheBrowser() throws Exception {
        when(logInHandler.handle(new LogInCommand(new Login("Zed"), new Password("password1"), new RememberMe(false))))
                .thenReturn(FORGOTTEN);

        mvc.perform(json(post("/api/auth/login"), "{\"login\": \"Zed\", \"password\": \"password1\"}"))
                .andExpect(status().isNoContent())
                .andExpect(header().stringValues(
                                HttpHeaders.SET_COOKIE,
                                hasItem(allOf(
                                        containsString("refresh_token=refresh-secret"),
                                        not(containsString("Max-Age"))))));
    }

    @Test
    void answers422ForWrongCredentials() throws Exception {
        when(logInHandler.handle(any())).thenThrow(new CredentialsInvalidException());

        mvc.perform(json(post("/api/auth/login"), "{\"login\": \"Zed\", \"password\": \"password2\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("user.credentials-invalid"));
    }

    @Test
    void answers422ForAnUnconfirmedAccount() throws Exception {
        when(logInHandler.handle(any())).thenThrow(new UserUnconfirmedException());

        mvc.perform(json(post("/api/auth/login"), "{\"login\": \"Zed\", \"password\": \"password1\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("user.unconfirmed"));
    }

    @Test
    void answers400ForAnEmptyLogin() throws Exception {
        mvc.perform(json(post("/api/auth/login"), "{\"login\": \"\", \"password\": \"password1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("user.login-required"));
    }

    @Test
    void refreshesTheSessionByItsCookie() throws Exception {
        when(refreshHandler.handle(new RefreshSessionCommand(new RefreshSecret("old-secret"))))
                .thenReturn(REMEMBERED);

        mvc.perform(post("/api/auth/refresh").cookie(new Cookie("refresh_token", "old-secret")))
                .andExpect(status().isNoContent())
                .andExpect(header().stringValues(
                                HttpHeaders.SET_COOKIE, hasItem(containsString("refresh_token=refresh-secret"))))
                .andExpect(header().stringValues(
                                HttpHeaders.SET_COOKIE, hasItem(containsString("access_token=access-jwt"))));
    }

    @Test
    void answers401WhenTheBrowserHasNoSession() throws Exception {
        mvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.session-expired"));
        verify(refreshHandler, never()).handle(any());
    }

    @Test
    void answers401WhenTheSessionIsOver() throws Exception {
        when(refreshHandler.handle(any())).thenThrow(new SessionExpiredException());

        mvc.perform(post("/api/auth/refresh").cookie(new Cookie("refresh_token", "old-secret")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.session-expired"));
    }

    @Test
    void signingOutEndsTheSessionAndClearsTheCookies() throws Exception {
        mvc.perform(post("/api/auth/logout").cookie(new Cookie("refresh_token", "old-secret")))
                .andExpect(status().isNoContent())
                .andExpect(header().stringValues(
                                HttpHeaders.SET_COOKIE,
                                hasItem(allOf(containsString("access_token=;"), containsString("Max-Age=0")))))
                .andExpect(header().stringValues(
                                HttpHeaders.SET_COOKIE,
                                hasItem(allOf(
                                        containsString("refresh_token=;"),
                                        containsString("Path=/api/auth"),
                                        containsString("Max-Age=0")))));

        verify(logOutHandler).handle(new LogOutCommand(new RefreshSecret("old-secret")));
    }

    @Test
    void signingOutWithoutASessionStillClearsTheCookies() throws Exception {
        mvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(containsString("Max-Age=0"))));

        verify(logOutHandler).handle(new LogOutCommand(null));
    }

    @Test
    void tellsTheUserOfTheAccessToken() throws Exception {
        when(currentUserHandler.handle(new GetCurrentUserQuery(
                        new Actor(new UserId(ID), Set.of(UserGroup.ADMIN, UserGroup.MEMBER)), null)))
                .thenReturn(Optional.of(new CurrentUser(
                        ID, "Zed", "zed@example.com", List.of("admin", "member"), List.of("add-entries"))));

        mvc.perform(get("/api/auth/me")
                        .cookie(new Cookie("access_token", accessToken(Instant.now(), "admin", "member"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.name").value("Zed"))
                .andExpect(jsonPath("$.email").value("zed@example.com"))
                .andExpect(jsonPath("$.groups[0]").value("admin"))
                .andExpect(jsonPath("$.groups[1]").value("member"))
                .andExpect(jsonPath("$.abilities[0]").value("add-entries"));
    }

    @Test
    void answersNoContentForAPublicVisitor() throws Exception {
        when(currentUserHandler.handle(new GetCurrentUserQuery(Actor.anonymous(), null)))
                .thenReturn(Optional.empty());

        mvc.perform(get("/api/auth/me")).andExpect(status().isNoContent());
    }

    @Test
    void anExpiredAccessTokenIsAPublicVisitor() throws Exception {
        when(currentUserHandler.handle(new GetCurrentUserQuery(Actor.anonymous(), null)))
                .thenReturn(Optional.empty());

        mvc.perform(get("/api/auth/me")
                        .cookie(new Cookie(
                                "access_token", accessToken(Instant.now().minus(1, ChronoUnit.HOURS), "admin"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void aForgedAccessTokenIsAPublicVisitor() throws Exception {
        when(currentUserHandler.handle(new GetCurrentUserQuery(Actor.anonymous(), null)))
                .thenReturn(Optional.empty());

        mvc.perform(get("/api/auth/me").cookie(new Cookie("access_token", "forged")))
                .andExpect(status().isNoContent());
    }

    @Test
    void asksToRefreshTheSessionTheBrowserHolds() throws Exception {
        when(currentUserHandler.handle(new GetCurrentUserQuery(Actor.anonymous(), new RefreshSecret("old-secret"))))
                .thenThrow(new AuthenticationRequiredException("auth.required", "Sign in first."));

        mvc.perform(get("/api/auth/me").cookie(new Cookie("refresh_token", "old-secret")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.required"));
    }

    private String accessToken(Instant issuedAt, String... groups) {
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(SecurityConfig.secretKey(properties)));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(ID.toString())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(15, ChronoUnit.MINUTES))
                .claim("groups", List.of(groups))
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
