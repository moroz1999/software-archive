package com.archive.backend.user.app;

import com.archive.backend.user.domain.Registrations;
import com.archive.backend.user.domain.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateAdministratorHandler {

    private final UserRepository users;
    private final Registrations registrations;
    private final Clock clock;

    @Transactional
    public void handle(CreateAdministratorCommand c) {
        users.save(registrations.registerAdministrator(c.name(), c.email(), c.password(), clock.instant()));
    }
}
