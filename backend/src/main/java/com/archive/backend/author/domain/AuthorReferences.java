package com.archive.backend.author.domain;

import com.archive.backend.label.domain.LabelId;

/** References to a person that can still point to the label from which the person is made. */
public interface AuthorReferences {

    /** Replaces the label with the person in every reference and removes the label. */
    void replaceLabel(LabelId label, AuthorId author);
}
