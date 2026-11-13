package com.archive.backend.label.domain;

/**
 * Labels of nobody as domain entities, for the use cases that add them. A label of an owner is changed through its
 * owner.
 */
public interface LabelRepository {

    void save(Label label);
}
