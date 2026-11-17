package com.archive.backend.user.web.mapper;

import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.user.app.ConfirmEmailCommand;
import com.archive.backend.user.app.GetCurrentUserQuery;
import com.archive.backend.user.app.LogInCommand;
import com.archive.backend.user.app.LogOutCommand;
import com.archive.backend.user.app.RefreshSessionCommand;
import com.archive.backend.user.app.RegisterUserCommand;
import com.archive.backend.user.app.ResendConfirmationCommand;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.InterfaceLanguage;
import com.archive.backend.user.domain.Login;
import com.archive.backend.user.domain.Password;
import com.archive.backend.user.domain.UserName;
import com.archive.backend.user.domain.session.IssuedSession;
import com.archive.backend.user.domain.session.RefreshSecret;
import com.archive.backend.user.domain.session.RememberMe;
import com.archive.backend.user.domain.session.SessionExpiredException;
import com.archive.backend.user.read.CurrentUser;
import com.archive.backend.user.web.dto.ConfirmRequestDto;
import com.archive.backend.user.web.dto.CurrentUserDto;
import com.archive.backend.user.web.dto.LogInRequestDto;
import com.archive.backend.user.web.dto.RegisterRequestDto;
import com.archive.backend.user.web.dto.ResendConfirmationRequestDto;
import com.archive.backend.user.web.dto.SessionDto;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthWebMapper {

    CurrentUserDto toCurrentUser(CurrentUser user);

    default SessionDto toSessionDto(IssuedSession session) {
        return new SessionDto(session.access().value());
    }

    default RegisterUserCommand toRegister(RegisterRequestDto request) {
        return new RegisterUserCommand(
                new UserName(required(request.name(), "user.name-required")),
                new Email(required(request.email(), "user.email-required")),
                toPassword(request.password()),
                toLanguage(request.language()));
    }

    default ConfirmEmailCommand toConfirm(ConfirmRequestDto request) {
        return new ConfirmEmailCommand(
                new ConfirmationToken(required(request.token(), "user.confirmation-token-required")));
    }

    default LogInCommand toLogIn(LogInRequestDto request) {
        return new LogInCommand(
                toLogin(request.login()),
                toPassword(request.password()),
                new RememberMe(Boolean.TRUE.equals(request.remember())));
    }

    default ResendConfirmationCommand toResend(ResendConfirmationRequestDto request) {
        return new ResendConfirmationCommand(
                toLogin(request.login()), toPassword(request.password()), toLanguage(request.language()));
    }

    /**
     * A browser without a session cookie has no session to refresh.
     */
    default RefreshSessionCommand toRefresh(@Nullable String cookie) {
        RefreshSecret secret = toSecret(cookie);
        if (secret == null) {
            throw new SessionExpiredException();
        }
        return new RefreshSessionCommand(secret);
    }

    default GetCurrentUserQuery toCurrentUserQuery(Actor actor, @Nullable String refreshCookie) {
        return new GetCurrentUserQuery(actor, toSecret(refreshCookie));
    }

    default LogOutCommand toLogOut(@Nullable String cookie) {
        return new LogOutCommand(toSecret(cookie));
    }

    // Request scalars -> value objects.

    private static @Nullable RefreshSecret toSecret(@Nullable String cookie) {
        return cookie == null || cookie.isBlank() ? null : new RefreshSecret(cookie);
    }

    private static Login toLogin(@Nullable String value) {
        return new Login(required(value, "user.login-required"));
    }

    private static Password toPassword(@Nullable String value) {
        return new Password(required(value, "user.password-too-short"));
    }

    private static InterfaceLanguage toLanguage(@Nullable String value) {
        return value == null ? InterfaceLanguage.EN : InterfaceLanguage.ofCode(value);
    }

    private static String required(@Nullable String value, String code) {
        if (value == null) {
            throw new InvalidValueException(code, "A required field is missing.");
        }
        return value;
    }
}
