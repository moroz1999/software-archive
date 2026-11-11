package com.archive.backend.group.app;

import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

class InMemoryGroupRepository implements GroupRepository {

    private final Map<GroupId, Group> items = new LinkedHashMap<>();

    int count() {
        return items.size();
    }

    @Override
    public Optional<Group> findById(GroupId id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public void save(Group group) {
        items.put(group.getId(), group);
    }

    @Override
    public void delete(Group group) {
        items.remove(group.getId());
    }
}
