package com.archive.backend.label.app;

import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.user.domain.Actor;

/** Makes a label found out to be another name of a group a label of it (docs/domain/authors.md#label). */
public record GiveLabelToGroupCommand(Actor actor, LabelId label, GroupId group) {}
