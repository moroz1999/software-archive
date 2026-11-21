package com.archive.backend.software.domain;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.domain.LabelId;

/** The credits of software given under a label. */
public interface LabelCredits {

    /** Credits the person directly wherever the label was credited. */
    void transfer(LabelId label, AuthorId author);

    /** Credits the group directly wherever the label was credited. */
    void transfer(LabelId label, GroupId group);
}
