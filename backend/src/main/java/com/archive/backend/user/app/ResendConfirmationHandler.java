package com.archive.backend.user.app;

import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.CredentialsInvalidException;
import com.archive.backend.user.domain.PasswordHasher;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mails a new confirmation link to an unconfirmed account; the old link stops working.
 */
@Service
@RequiredArgsConstructor
public class ResendConfirmationHandler {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    @Transactional
    public void handle(ResendConfirmationCommand c) {
        User user = users.findByLogin(c.login()).orElseThrow(CredentialsInvalidException::new);
        user.verifyPassword(c.password(), hasher);
        user.renewConfirmation(ConfirmationToken.generate(), clock.instant(), c.language());
        users.save(user);
        user.pullEvents().forEach(events::publishEvent);
    }
}
