package com.archive.backend.user.app;

import com.archive.backend.user.domain.UserSearch;
import com.archive.backend.user.read.UserEntry;
import com.archive.backend.user.read.UserReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListUsersHandler {

    private final UserReadRepository reads;

    @Transactional(readOnly = true)
    public Page<UserEntry> handle(ListUsersQuery q) {
        q.actor().requireAllowed(q.actor().mayManageUsers());
        UserSearch search = q.search();
        return reads.findPage(search == null ? null : search.value(), q.pageable());
    }
}
