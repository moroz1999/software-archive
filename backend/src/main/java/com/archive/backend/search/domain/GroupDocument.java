package com.archive.backend.search.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * What the search knows of a group (docs/features/search.md#collections): the main name, every label, the name of
 * the group, and the platforms of the software its labels are credited on.
 */
@ValueObject
public record GroupDocument(UUID id, String name, List<String> labels, String groupName, List<String> platforms)
        implements SearchDocument {

    public GroupDocument {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(groupName, "groupName");
        labels = List.copyOf(labels);
        platforms = List.copyOf(platforms);
    }

    @Override
    public SearchEntity entity() {
        return new SearchEntity(SearchEntityType.GROUP, id);
    }
}
