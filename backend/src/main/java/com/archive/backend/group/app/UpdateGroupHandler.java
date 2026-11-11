package com.archive.backend.group.app;

import com.archive.backend.geo.domain.Places;
import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupNotFoundException;
import com.archive.backend.group.domain.GroupRepository;
import com.archive.backend.group.domain.GroupTypes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateGroupHandler {

    private final GroupRepository repository;
    private final GroupTypes groupTypes;
    private final Places places;

    @Transactional
    public void handle(UpdateGroupCommand c) {
        Group group = repository.findById(c.id()).orElseThrow(GroupNotFoundException::new);
        group.edit(c.name(), c.notes(), groupTypes.of(c.types()), c.labels(), places.locations(c.locations()));
        repository.save(group);
    }
}
