package com.archive.backend.search.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * What the search knows of a person or of a label of nobody (docs/features/search.md#collections): the main name,
 * every label, the real name, and the platforms of the software their labels are credited on.
 */
@ValueObject
public record AuthorDocument(
        UUID id,
        Kind kind,
        String name,
        List<String> labels,
        @Nullable String realName,
        List<String> platforms) implements SearchDocument {

    /** Whom a document is of: a person, or a label nobody is known to be behind. */
    public enum Kind {
        PERSON,
        LABEL
    }

    public AuthorDocument {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(name, "name");
        labels = List.copyOf(labels);
        platforms = List.copyOf(platforms);
    }

    /** A person under the main name, with every label and the real name. */
    public static AuthorDocument person(
            UUID id, String name, List<String> labels, @Nullable String realName, List<String> platforms) {
        return new AuthorDocument(id, Kind.PERSON, name, labels, realName, platforms);
    }

    /** A label of nobody under its own name, with the platforms of the software crediting it. */
    public static AuthorDocument label(UUID id, String name, List<String> platforms) {
        return new AuthorDocument(id, Kind.LABEL, name, List.of(name), null, platforms);
    }

    @Override
    public SearchEntity entity() {
        return new SearchEntity(SearchEntityType.AUTHOR, id);
    }
}
