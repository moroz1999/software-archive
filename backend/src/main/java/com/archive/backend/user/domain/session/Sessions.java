package com.archive.backend.user.domain.session;

import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserRepository;
import java.time.Instant;

/**
 * Opens and refreshes sessions. A refresh secret works once: refreshing replaces it with a new one, and the
 * new access token tells the groups the user has by then.
 */
public final class Sessions {

    private final SessionRepository sessions;
    private final UserRepository users;
    private final AccessTokens accessTokens;

    public Sessions(SessionRepository sessions, UserRepository users, AccessTokens accessTokens) {
        this.sessions = sessions;
        this.users = users;
        this.accessTokens = accessTokens;
    }

    public IssuedSession open(User user, RememberMe remember, Instant now) {
        RefreshSecret secret = RefreshSecret.generate();
        Session session = Session.open(secret, user.getId(), remember, now);
        sessions.save(session);
        return new IssuedSession(accessTokens.issue(user, now), secret, remember, session.getExpiresAt());
    }

    public IssuedSession refresh(RefreshSecret secret, Instant now) {
        Session session = sessions.findByDigest(secret.digest()).orElseThrow(SessionExpiredException::new);
        sessions.delete(session);
        if (session.isExpired(now)) {
            throw new SessionExpiredException();
        }
        User user = users.findById(session.getUserId()).orElseThrow(SessionExpiredException::new);
        return open(user, session.getRemember(), now);
    }
}
