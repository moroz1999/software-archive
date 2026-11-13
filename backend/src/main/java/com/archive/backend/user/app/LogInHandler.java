package com.archive.backend.user.app;

import com.archive.backend.user.domain.CredentialsInvalidException;
import com.archive.backend.user.domain.PasswordHasher;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserRepository;
import com.archive.backend.user.domain.session.IssuedSession;
import com.archive.backend.user.domain.session.Sessions;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogInHandler {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final Sessions sessions;
    private final Clock clock;

    @Transactional
    public IssuedSession handle(LogInCommand c) {
        User user = users.findByLogin(c.login()).orElseThrow(CredentialsInvalidException::new);
        user.authenticate(c.password(), hasher);
        return sessions.open(user, c.remember(), clock.instant());
    }
}
