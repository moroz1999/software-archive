package com.archive.backend.group.domain;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.label.domain.LabelId;

/** The memberships in groups held under a label. */
public interface LabelMemberships {

    /**
     * Makes the person the member wherever the label was one; a group the person is already a member of keeps that
     * one membership, with the roles of both.
     */
    void transfer(LabelId label, AuthorId author);

    /**
     * Makes the group a member group wherever the label was a member; a group member has no roles, so the label's
     * are not kept.
     */
    void transfer(LabelId label, GroupId group);

    /**
     * Makes the other label the member wherever the label was one; a group it is already a member of keeps that one
     * membership, with the roles of both.
     */
    void transfer(LabelId label, LabelId into);
}
