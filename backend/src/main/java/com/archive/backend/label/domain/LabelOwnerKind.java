package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** Whose a label can be: a person or a group. */
@ValueObject
public enum LabelOwnerKind {
    AUTHOR,
    GROUP
}
