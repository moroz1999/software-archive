package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * An account of the archive (docs/domain/users.md).
 */
@AggregateRoot
@Getter
public final class User {

    /**
     * How long a confirmation link works.
     */
    public static final Duration CONFIRMATION_LIFETIME = Duration.ofDays(7);

    private final UserId id;
    private final UserName name;
    private final Email email;
    private final PasswordHash passwordHash;
    private final Instant registeredAt;
    private UserStatus status;
    private final Set<UserGroup> groups;
    private @Nullable PendingConfirmation confirmation;

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

    public User(
            UserId id,
            UserName name,
            Email email,
            PasswordHash passwordHash,
            Instant registeredAt,
            UserStatus status,
            Set<UserGroup> groups,
            @Nullable PendingConfirmation confirmation) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.email = Objects.requireNonNull(email, "email");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.registeredAt = Objects.requireNonNull(registeredAt, "registeredAt");
        this.status = Objects.requireNonNull(status, "status");
        this.groups = groups.isEmpty() ? EnumSet.noneOf(UserGroup.class) : EnumSet.copyOf(groups);
        this.confirmation = confirmation;
    }

    /**
     * A new unconfirmed account, waiting for its confirmation link, mailed in the given language.
     */
    public static User register(
            UserId id,
            UserName name,
            Email email,
            PasswordHash hash,
            ConfirmationToken token,
            Instant now,
            InterfaceLanguage language) {
        User user = new User(id, name, email, hash, now, UserStatus.UNCONFIRMED, Set.of(), pendingFor(token, now));
        user.events.add(new ConfirmationLinkIssued(email, name, token, language));
        return user;
    }

    /**
     * Replaces the confirmation link of an unconfirmed account with a new one.
     */
    public void renewConfirmation(ConfirmationToken token, Instant now, InterfaceLanguage language) {
        if (status == UserStatus.CONFIRMED) {
            throw new UserAlreadyConfirmedException();
        }
        confirmation = pendingFor(token, now);
        events.add(new ConfirmationLinkIssued(email, name, token, language));
    }

    /**
     * Confirms the account by the secret of its link; a confirmed account is a member.
     */
    public void confirm(ConfirmationToken token, Instant now) {
        PendingConfirmation pending = confirmation;
        if (pending == null || !pending.digest().equals(token.digest())) {
            throw new ConfirmationInvalidException();
        }
        if (!now.isBefore(pending.expiresAt())) {
            throw new ConfirmationExpiredException();
        }
        status = UserStatus.CONFIRMED;
        groups.add(UserGroup.MEMBER);
        confirmation = null;
    }

    public void grant(UserGroup group) {
        groups.add(group);
    }

    /**
     * Gives a confirmed user the groups an administrator chose; every confirmed user stays a member.
     */
    public void changeGroups(GrantedGroups granted) {
        if (status != UserStatus.CONFIRMED) {
            throw new UnconfirmedUserGroupsException();
        }
        groups.clear();
        groups.add(UserGroup.MEMBER);
        groups.addAll(granted.groups());
    }

    /**
     * Checks the password only.
     */
    public void verifyPassword(Password password, PasswordHasher hasher) {
        if (!hasher.matches(password, passwordHash)) {
            throw new CredentialsInvalidException();
        }
    }

    /**
     * Checks the user may sign in with the password.
     */
    public void authenticate(Password password, PasswordHasher hasher) {
        verifyPassword(password, hasher);
        if (status != UserStatus.CONFIRMED) {
            throw new UserUnconfirmedException();
        }
    }

    /**
     * Unconfirmed, with an expired link: it holds neither its name nor its email any more.
     */
    public boolean isAbandoned(Instant now) {
        PendingConfirmation pending = confirmation;
        return status == UserStatus.UNCONFIRMED && pending != null && !now.isBefore(pending.expiresAt());
    }

    private static PendingConfirmation pendingFor(ConfirmationToken token, Instant now) {
        return new PendingConfirmation(token.digest(), now.plus(CONFIRMATION_LIFETIME));
    }

    public Set<UserGroup> getGroups() {
        return Set.copyOf(groups);
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    public Actor actor() {
        return new Actor(id, getGroups());
    }
}
