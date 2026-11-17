package com.archive.backend.label.domain;

import java.util.Optional;

/**
 * Labels as domain entities: their names, locations and adders. Whose a label is changes through its owner.
 */
public interface LabelRepository {

    /** Adds a new label of nobody, or stores the name and the locations of one in the archive. */
    void save(Label label);

    /** The label, of nobody or of an owner, kept from any other change until the transaction ends. */
    Optional<Label> lock(LabelId id);

    /** The label, while it is of nobody, kept from any other change until the transaction ends. */
    Optional<Label> lockOfNobody(LabelId id);

    /** Whether the label is in the archive, of nobody or of an owner. */
    boolean exists(LabelId id);

    /** Deletes the label with its locations; its credits and memberships go with it. */
    void delete(Label label);
}
