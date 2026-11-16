package com.archive.backend.label.app;

import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.user.domain.Actor;
import java.util.Set;

/** Makes a new group of the given types under the name of a label (docs/domain/authors.md#label). */
public record MakeGroupOfLabelCommand(Actor actor, LabelId label, Set<GroupTypeCode> types) {}
