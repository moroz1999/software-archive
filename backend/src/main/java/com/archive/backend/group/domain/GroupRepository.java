package com.archive.backend.group.domain;

import com.archive.backend.label.domain.LabelId;
import java.util.Optional;

/**
 * Groups as domain entities, for the use cases that change them. Reads go through the read model.
 */
public interface GroupRepository {

    Optional<Group> findById(GroupId id);

    /** The group a label belongs to, if it belongs to one. */
    Optional<Group> findByLabel(LabelId label);

    void save(Group group);

    /**
     * Deletes the group with its types, locations and memberships; its labels stay in the archive as labels of
     * nobody, with their credits.
     */
    void delete(Group group);
}
