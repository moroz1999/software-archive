package com.archive.backend.group.app;

import com.archive.backend.group.domain.GroupId;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.Actor;

public record GetGroupQuery(Actor actor, GroupId id, Language language) {}
