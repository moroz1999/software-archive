package com.archive.backend.group.app;

import com.archive.backend.geo.domain.Places;
import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupNotFoundException;
import com.archive.backend.group.domain.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Fills the empty fields of a group from an import (docs/domain/imports.md#authors). */
@Service
@RequiredArgsConstructor
public class SupplementGroupHandler {

    private final GroupRepository repository;
    private final Places places;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(SupplementGroupCommand c) {
        Group group = repository.findById(c.id()).orElseThrow(GroupNotFoundException::new);
        group.requireChangeableBy(c.actor());
        group.supplement(places.locations(c.locations()));
        repository.save(group);
        group.pullEvents().forEach(events::publishEvent);
    }
}
