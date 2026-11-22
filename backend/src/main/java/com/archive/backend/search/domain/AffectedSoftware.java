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

    /** The software the person is credited on by themselves, not by a label. */
    List<UUID> creditingPerson(UUID person);

    /** The software the group is credited on by itself, not by a label. */
    List<UUID> creditingGroup(UUID group);

    /**
     * The software credited, by themselves or by one of their labels, on the people and the groups located in the city:
     * the search tells its country by their places.
     */
    List<UUID> creditedFromCity(UUID city);

    /**
     * The software credited, by themselves or by one of their labels, on the people and the groups located in the
     * country, in it directly or in one of its cities.
     */
    List<UUID> creditedFromCountry(UUID country);
}
