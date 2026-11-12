package com.archive.backend.search.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The search documents of groups, built from the archive as it is now. */
public interface GroupDocuments {

    /** The document of the group; empty when the group is gone. */
    Optional<GroupDocument> of(UUID group);

    /** At most {@code limit} documents in the order of their ids, after the id {@code after} (from the first without). */
    List<GroupDocument> after(@Nullable UUID after, int limit);
}
