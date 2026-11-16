package com.archive.backend.search.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The search documents of people and of labels of nobody, built from the archive as it is now. */
public interface AuthorDocuments {

    /**
     * The document of the person or of the label of nobody; empty when it is gone, the person has no name, or the label
     * has an owner.
     */
    Optional<AuthorDocument> of(UUID author);

    /** At most {@code limit} documents in the order of their ids, after the id {@code after} (from the first without). */
    List<AuthorDocument> after(@Nullable UUID after, int limit);
}
