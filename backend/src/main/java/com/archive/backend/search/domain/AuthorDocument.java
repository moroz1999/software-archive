package com.archive.backend.search.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * What the search knows of a person (docs/features/search.md#collections): the main name, every label, the real name,
 * and the platforms of the software their labels are credited on.
 */
@ValueObject
public record AuthorDocument(
        UUID id, String name, List<String> labels, @Nullable String realName, List<String> platforms)
        implements SearchDocument {

    public AuthorDocument {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        labels = List.copyOf(labels);
        platforms = List.copyOf(platforms);
    }

    @Override
    public SearchEntity entity() {
        return new SearchEntity(SearchEntityType.AUTHOR, id);
    }
}
