package com.archive.backend.user.app;

import com.archive.backend.user.domain.Registrations;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserHandler {

    private final UserRepository users;
    private final Registrations registrations;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    @Transactional
    public void handle(RegisterUserCommand c) {
        User user = registrations.register(c.name(), c.email(), c.password(), clock.instant(), c.language());
        users.save(user);
        user.pullEvents().forEach(events::publishEvent);
    }
}
