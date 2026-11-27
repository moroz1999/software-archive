package com.archive.backend.user.web;

import com.archive.backend.user.app.ConfirmEmailHandler;
import com.archive.backend.user.app.GetCurrentUserHandler;
import com.archive.backend.user.app.LogInHandler;
import com.archive.backend.user.app.LogOutHandler;
import com.archive.backend.user.app.RefreshSessionHandler;
import com.archive.backend.user.app.RegisterUserHandler;
import com.archive.backend.user.app.ResendConfirmationHandler;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.session.IssuedSession;
import com.archive.backend.user.web.dto.ConfirmRequestDto;
import com.archive.backend.user.web.dto.CurrentUserDto;
import com.archive.backend.user.web.dto.LogInRequestDto;
import com.archive.backend.user.web.dto.RegisterRequestDto;
import com.archive.backend.user.web.dto.ResendConfirmationRequestDto;
import com.archive.backend.user.web.dto.SessionDto;
import com.archive.backend.user.web.mapper.AuthWebMapper;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserHandler registerHandler;
    private final ConfirmEmailHandler confirmHandler;
    private final ResendConfirmationHandler resendHandler;
    private final LogInHandler logInHandler;
    private final RefreshSessionHandler refreshHandler;
    private final LogOutHandler logOutHandler;
    private final GetCurrentUserHandler currentUserHandler;
    private final AuthWebMapper webMapper;
    private final SessionCookies cookies;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequestDto request) {
        registerHandler.handle(webMapper.toRegister(request));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<SessionDto> confirm(@RequestBody ConfirmRequestDto request) {
        IssuedSession session = confirmHandler.handle(webMapper.toConfirm(request));
        return ResponseEntity.ok().headers(cookies.issue(session)).body(webMapper.toSessionDto(session));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/resend-confirmation")
    public ResponseEntity<Void> resendConfirmation(@RequestBody ResendConfirmationRequestDto request) {
        resendHandler.handle(webMapper.toResend(request));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<SessionDto> logIn(@RequestBody LogInRequestDto request) {
        IssuedSession session = logInHandler.handle(webMapper.toLogIn(request));
        return ResponseEntity.ok().headers(cookies.issue(session)).body(webMapper.toSessionDto(session));
    }

    /** Replaces the refresh cookie and returns a new access token; without the cookie there is nothing to refresh. */
    @PostMapping("/refresh")
    public ResponseEntity<SessionDto> refresh(
            @CookieValue(name = SessionCookies.REFRESH_COOKIE, required = false) @Nullable String secret) {
        IssuedSession session = refreshHandler.handle(webMapper.toRefresh(secret));
        return ResponseEntity.ok().headers(cookies.issue(session)).body(webMapper.toSessionDto(session));
    }

    /**
     * Ends the session and clears its refresh cookie, also when the browser holds no session any more.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    public ResponseEntity<Void> logOut(
            @CookieValue(name = SessionCookies.REFRESH_COOKIE, required = false) @Nullable String secret) {
        logOutHandler.handle(webMapper.toLogOut(secret));
        return ResponseEntity.noContent().headers(cookies.clear()).build();
    }

    /**
     * The signed-in user; no content for a public visitor with no session to refresh.
     */
    @GetMapping("/me")
    @ApiResponse(responseCode = "200", description = "The signed-in user")
    @ApiResponse(responseCode = "204", description = "A visitor with no session", content = @Content)
    public ResponseEntity<CurrentUserDto> me(
            Actor actor,
            @CookieValue(name = SessionCookies.REFRESH_COOKIE, required = false) @Nullable String refresh) {
        return currentUserHandler
                .handle(webMapper.toCurrentUserQuery(actor, refresh))
                .map(user -> ResponseEntity.ok(webMapper.toCurrentUser(user)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
