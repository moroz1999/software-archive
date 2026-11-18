package com.archive.backend.label.app;

import com.archive.backend.label.domain.LabelId;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.Actor;

public record GetLabelQuery(Actor actor, LabelId id, Language language) {}
