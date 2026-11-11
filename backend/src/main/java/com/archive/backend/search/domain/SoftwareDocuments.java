package com.archive.backend.search.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The search documents of software, built from the archive as it is now. */
public interface SoftwareDocuments {

    /** The document of the software; empty when the software is gone. */
    Optional<SoftwareDocument> of(UUID software);

    /** At most {@code limit} documents in the order of their ids, after the id {@code after} (from the first without). */
    List<SoftwareDocument> after(@Nullable UUID after, int limit);
}
