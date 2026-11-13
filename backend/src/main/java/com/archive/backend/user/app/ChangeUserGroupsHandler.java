package com.archive.backend.user.app;

import com.archive.backend.user.domain.GroupAssignment;
import com.archive.backend.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeUserGroupsHandler {

    private final UserRepository users;
    private final GroupAssignment assignment;

    @Transactional
    public void handle(ChangeUserGroupsCommand c) {
        users.save(assignment.assign(c.actor(), c.id(), c.groups()));
    }
}
