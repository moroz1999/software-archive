package com.archive.backend.group.app;

import com.archive.backend.group.domain.GroupNotFoundException;
import com.archive.backend.group.read.GroupDetails;
import com.archive.backend.group.read.GroupReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetGroupHandler {

    private final GroupReadRepository reads;

    @Transactional(readOnly = true)
    public GroupDetails handle(GetGroupQuery q) {
        return reads.findById(q.id().value()).orElseThrow(GroupNotFoundException::new);
    }
}
