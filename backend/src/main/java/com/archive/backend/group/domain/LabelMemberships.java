package com.archive.backend.group.domain;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.label.domain.LabelId;

/** The memberships in groups held under a label. */
public interface LabelMemberships {

    /** Makes the person the member wherever the label was one. */
    void transfer(LabelId label, AuthorId author);

    /**
     * Makes the group a member group wherever the label was a member; a group member has no roles or dates, so the
     * label's are not kept.
     */
    void transfer(LabelId label, GroupId group);
}
