package com.archive.backend.user.app;

import com.archive.backend.user.domain.UserEditing;
import com.archive.backend.user.domain.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EditUserHandler {

    private final UserRepository users;
    private final UserEditing editing;
    private final Clock clock;

    @Transactional
    public void handle(EditUserCommand c) {
        users.save(editing.edit(c.actor(), c.id(), c.name(), c.email(), c.groups(), clock.instant()));
    }
}
