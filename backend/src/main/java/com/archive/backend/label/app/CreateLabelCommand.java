package com.archive.backend.label.app;

import com.archive.backend.label.domain.LabelName;
import com.archive.backend.user.domain.Actor;

public record CreateLabelCommand(Actor actor, LabelName name) {}
