package com.archive.backend.user;

import com.archive.backend.shared.web.AccessTokenCookieFilter;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.InterfaceLanguage;
import com.archive.backend.user.domain.PasswordHash;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserName;
import com.archive.backend.user.domain.session.AccessTokens;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

/** Who acts in tests: an administrator, who may change everything, and two members. */
public final class TestActors {

    public static final UserId ADMIN_ID = new UserId(UUID.fromString("00000000-0000-0000-00aa-000000000001"));
    public static final UserId MEMBER_ID = new UserId(UUID.fromString("00000000-0000-0000-00aa-000000000002"));
    public static final UserId OTHER_ID = new UserId(UUID.fromString("00000000-0000-0000-00aa-000000000003"));

    public static final Actor ADMIN = new Actor(ADMIN_ID, Set.of(UserGroup.ADMIN, UserGroup.MEMBER));
    public static final Actor MEMBER = new Actor(MEMBER_ID, Set.of(UserGroup.MEMBER));
    public static final Actor OTHER = new Actor(OTHER_ID, Set.of(UserGroup.MEMBER));

    private TestActors() {}

    /** Stores their accounts, for tests whose entries refer to whoever added them. */
    public static void store(JdbcTemplate jdbc) {
        store(jdbc, ADMIN_ID, "TestAdmin");
        store(jdbc, MEMBER_ID, "TestMember");
        store(jdbc, OTHER_ID, "TestOther");
    }

    /** The access cookie of a signed-in actor, for requests through the web layer. */
    public static Cookie accessCookie(AccessTokens tokens, Actor actor) {
        UserId id = actor.signedIn();
        ConfirmationToken token = new ConfirmationToken("test-" + id.value());
        Instant now = Instant.now();
        User user = User.register(
                id,
                new UserName("Test" + id.value().toString().substring(30)),
                new Email(id.value() + "@archive.test"),
                new PasswordHash("hash"),
                token,
                now,
                InterfaceLanguage.EN);
        user.confirm(token, now);
        actor.groups().forEach(user::grant);
        return new Cookie(
                AccessTokenCookieFilter.COOKIE, tokens.issue(user, now).value());
    }

    /** Removes their accounts, for tests that commit for real. */
    public static void remove(JdbcTemplate jdbc) {
        jdbc.update(
                "DELETE FROM app_user WHERE id IN (?, ?, ?)", ADMIN_ID.value(), MEMBER_ID.value(), OTHER_ID.value());
    }

    private static void store(JdbcTemplate jdbc, UserId id, String name) {
        jdbc.update("""
                        INSERT INTO app_user (id, name, email, password_hash, registered_at, status)
                        VALUES (?, ?, ?, 'hash', now(), 'CONFIRMED')
                        ON CONFLICT (id) DO NOTHING""", id.value(), name, name.toLowerCase(Locale.ROOT) + "@archive.test");
    }
}
