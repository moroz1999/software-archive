package com.archive.backend.group.app;

import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.domain.LabelAttachment;
import com.archive.backend.user.domain.Actor;

/**
 * Gives a label of nobody to the group found out to be its owner (docs/domain/authors.md#label), as its display name
 * when the person attaching it says so.
 */
public record AttachLabelToGroupCommand(Actor actor, LabelAttachment label, GroupId group) {}
