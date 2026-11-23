package com.archive.backend.group.app;

import com.archive.backend.geo.domain.Places;
import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupMemberSets;
import com.archive.backend.group.domain.GroupRepository;
import com.archive.backend.group.domain.GroupTypes;
import com.archive.backend.user.domain.AddedBy;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateGroupHandler {

    private final GroupRepository repository;
    private final GroupTypes groupTypes;
    private final Places places;
    private final GroupMemberSets memberSets;
    private final ApplicationEventPublisher events;

    @Transactional
    public GroupId handle(CreateGroupCommand c) {
        AddedBy addedBy = AddedBy.of(c.actor());
        GroupId id = new GroupId(UUID.randomUUID());
        Group group = Group.create(
                id,
                c.name(),
                c.notes(),
                groupTypes.of(c.types()),
                c.labels(),
                places.locations(c.locations()),
                memberSets.of(c.members()),
                addedBy);
        repository.save(group);
        group.pullEvents().forEach(events::publishEvent);
        return group.getId();
    }
}
