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
}
