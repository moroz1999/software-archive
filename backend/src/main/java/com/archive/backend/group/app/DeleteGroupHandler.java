package com.archive.backend.group.app;

import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupNotFoundException;
import com.archive.backend.group.domain.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteGroupHandler {

    private final GroupRepository repository;

    @Transactional
    public void handle(DeleteGroupCommand c) {
        Group group = repository.findById(c.id()).orElseThrow(GroupNotFoundException::new);
        repository.delete(group);
    }
}
