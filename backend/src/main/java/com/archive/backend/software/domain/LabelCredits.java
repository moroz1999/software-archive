package com.archive.backend.software.domain;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.domain.LabelId;

/**
 * The credits of software and of releases given under a label. Wherever two credits come together, one credit stays,
 * with the roles of both (docs/domain/software.md#credits).
 */
public interface LabelCredits {

    /** Credits the person directly wherever the label was credited. */
    void transfer(LabelId label, AuthorId author);

    /** Credits the group directly wherever the label was credited. */
    void transfer(LabelId label, GroupId group);

    /** Credits the other label wherever the label was credited. */
    void transfer(LabelId label, LabelId into);

    /** Wherever the person is credited by themselves and under their label, only the credit under the label stays. */
    void takeIn(LabelId label, AuthorId owner);

    /** Wherever the group is credited by itself and under its label, only the credit under the label stays. */
    void takeIn(LabelId label, GroupId owner);
}
