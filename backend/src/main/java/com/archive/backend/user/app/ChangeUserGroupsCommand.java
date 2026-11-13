package com.archive.backend.user.app;

import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.GrantedGroups;
import com.archive.backend.user.domain.UserId;

public record ChangeUserGroupsCommand(Actor actor, UserId id, GrantedGroups groups) {}
