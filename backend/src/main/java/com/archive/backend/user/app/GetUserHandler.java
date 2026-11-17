package com.archive.backend.user.app;

import com.archive.backend.user.domain.UserNotFoundException;
import com.archive.backend.user.read.UserEntry;
import com.archive.backend.user.read.UserReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserHandler {

    private final UserReadRepository reads;

    @Transactional(readOnly = true)
    public UserEntry handle(GetUserQuery q) {
        q.actor().requireAllowed(q.actor().mayManageUsers());
        return reads.findEntry(q.id().value()).orElseThrow(UserNotFoundException::new);
    }
}
