package com.archive.backend.search.domain;

import java.util.List;
import java.util.UUID;

/**
 * The people, groups and labels of nobody whose documents a change of software reaches: those credited on it, by
 * themselves or by a label.
 */
public interface AffectedOwners {

    List<UUID> peopleCreditedOn(UUID software);

    List<UUID> groupsCreditedOn(UUID software);

    List<UUID> labelsOfNobodyCreditedOn(UUID software);

    /** The person the label belongs to, if any: their document names their labels. */
    List<UUID> peopleOwning(UUID label);

    /** The group the label belongs to, if any: its document names its labels. */
    List<UUID> groupsOwning(UUID label);
}
