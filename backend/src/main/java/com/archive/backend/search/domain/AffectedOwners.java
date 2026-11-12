package com.archive.backend.search.domain;

import java.util.List;
import java.util.UUID;

/** The people and groups whose documents a change of software reaches: the owners of its credited labels. */
public interface AffectedOwners {

    List<UUID> peopleCreditedOn(UUID software);

    List<UUID> groupsCreditedOn(UUID software);
}
