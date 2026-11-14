package com.archive.backend.search.domain;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** The software whose search documents a change elsewhere in the archive reaches. */
public interface AffectedSoftware {

    /** The software in the category or in any category below it. */
    List<UUID> inSubtreeOf(UUID category);

    /** The software any of the labels is credited on or names as its developer company. */
    List<UUID> creditedOn(Collection<UUID> labels);

    /** The software the person is credited on by themselves, not by a label. */
    List<UUID> creditingPerson(UUID person);

    /** The software that names the group, not a label of it, as its developer company. */
    List<UUID> developedByGroup(UUID group);
}
