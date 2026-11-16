package com.archive.backend.group.app;

import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.user.domain.Actor;

/** Gives a label of nobody to the group found out to be its owner (docs/domain/authors.md#label). */
public record AttachLabelToGroupCommand(Actor actor, LabelId label, GroupId group) {}
