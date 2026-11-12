package com.archive.backend.search.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The search documents of people, built from the archive as it is now. */
public interface AuthorDocuments {

    /** The document of the person; empty when the person is gone or has no name. */
    Optional<AuthorDocument> of(UUID person);

    /** At most {@code limit} documents in the order of their ids, after the id {@code after} (from the first without). */
    List<AuthorDocument> after(@Nullable UUID after, int limit);
}
