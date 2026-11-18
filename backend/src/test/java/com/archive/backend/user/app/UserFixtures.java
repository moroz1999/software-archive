package com.archive.backend.user.app;

import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.ConfirmationLinkIssued;
import com.archive.backend.user.domain.ConfirmationMailer;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.Password;
import com.archive.backend.user.domain.PasswordHash;
import com.archive.backend.user.domain.PasswordHasher;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserName;
import com.archive.backend.user.domain.session.AccessToken;
import com.archive.backend.user.domain.session.AccessTokens;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;

final class UserFixtures {

    static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    static final Password PASSWORD = new Password("password1");

    private UserFixtures() {}

    static UserId id(int n) {
        return new UserId(UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n)));
    }

    /** Keeps the password readable inside the hash, so tests see what was hashed. */
    static final PasswordHasher HASHER = new PasswordHasher() {
        @Override
        public PasswordHash hash(Password password) {
            return new PasswordHash("hashed:" + password.value());
        }

        @Override
        public boolean matches(Password password, PasswordHash hash) {
            return hash.value().equals("hashed:" + password.value());
        }
    };

    /** An access token that tells whose it is and their groups at the time it was issued. */
    static final AccessTokens ACCESS_TOKENS = (User user, Instant now) -> new AccessToken(
            "access:" + user.getId().value() + ":"
                    + user.getGroups().stream().map(UserGroup::code).sorted().collect(Collectors.joining(",")),
            now.plus(Duration.ofMinutes(15)));

    /** An account as stored: the letter of its registration went out long ago. */
    static User unconfirmed(int n, String name, ConfirmationToken token, Instant registeredAt) {
        User user = User.register(
                id(n),
                new UserName(name),
                new Email(name.toLowerCase(Locale.ROOT) + "@example.com"),
                HASHER.hash(PASSWORD),
                token,
                registeredAt,
                Language.EN);
        user.pullEvents();
        return user;
    }

    static User confirmed(int n, String name, UserGroup... extra) {
        ConfirmationToken token = new ConfirmationToken("token-" + n);
        User user = unconfirmed(n, name, token, NOW.minus(Duration.ofDays(1)));
        user.confirm(token, NOW.minus(Duration.ofDays(1)));
        for (UserGroup group : extra) {
            user.grant(group);
        }
        return user;
    }

    record Letter(Email to, UserName name, ConfirmationToken token, Language language) {}

    static final class RecordingMailer implements ConfirmationMailer {

        final List<Letter> letters = new ArrayList<>();

        @Override
        public void send(Email to, UserName name, ConfirmationToken token, Language language) {
            letters.add(new Letter(to, name, token, language));
        }
    }

    /** Publishes the events of a change to the handler that mails the links. */
    static ApplicationEventPublisher mailing(RecordingMailer mailer) {
        MailConfirmationLinkHandler handler = new MailConfirmationLinkHandler(mailer);
        return event -> {
            if (event instanceof ConfirmationLinkIssued issued) {
                handler.on(issued);
            }
        };
    }

    /** A clock that tests move forward. */
    static final class MovingClock extends Clock {

        private Instant now = NOW;

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
