package com.archive.backend.group.app;

import com.archive.backend.group.domain.GroupId;
import com.archive.backend.user.domain.Actor;

public record DeleteGroupCommand(Actor actor, GroupId id) {}
