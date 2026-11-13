package com.archive.backend.user.app;

import com.archive.backend.user.domain.AccountConfirmation;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserRepository;
import com.archive.backend.user.domain.session.IssuedSession;
import com.archive.backend.user.domain.session.RememberMe;
import com.archive.backend.user.domain.session.Sessions;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confirms the account and signs its user in.
 */
@Service
@RequiredArgsConstructor
public class ConfirmEmailHandler {

    private final UserRepository users;
    private final AccountConfirmation confirmation;
    private final Sessions sessions;
    private final Clock clock;

    @Transactional
    public IssuedSession handle(ConfirmEmailCommand c) {
        Instant now = clock.instant();
        User user = confirmation.confirm(c.token(), now);
        users.save(user);
        return sessions.open(user, new RememberMe(true), now);
    }
}
