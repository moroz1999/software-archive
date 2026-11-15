package com.archive.backend.label.domain;

import java.util.Optional;

/**
 * Labels of nobody as domain entities, for the use cases that add them. A label of an owner is changed through its
 * owner.
 */
public interface LabelRepository {

    void save(Label label);

    /** The label, while it is of nobody, kept from any other change until the transaction ends. */
    Optional<Label> lockOfNobody(LabelId id);

    /** Whether the label is in the archive, of nobody or of an owner. */
    boolean exists(LabelId id);
}
