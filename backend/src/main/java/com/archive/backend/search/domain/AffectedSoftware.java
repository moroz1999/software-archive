package com.archive.backend.search.domain;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** The software whose search documents a change elsewhere in the archive reaches. */
public interface AffectedSoftware {

    /** The software in the category or in any category below it. */
    List<UUID> inSubtreeOf(UUID category);

    /** The software any of the labels is credited on. */
    List<UUID> creditedOn(Collection<UUID> labels);
}
