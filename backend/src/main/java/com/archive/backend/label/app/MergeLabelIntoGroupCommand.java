package com.archive.backend.label.app;

import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.user.domain.Actor;

/** Merges a label found out to be a duplicate of a group into it (docs/domain/authors.md#label). */
public record MergeLabelIntoGroupCommand(Actor actor, LabelId label, GroupId group) {}
