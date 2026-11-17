package com.archive.backend.user.app;

import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.GrantedGroups;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserName;

public record EditUserCommand(Actor actor, UserId id, UserName name, Email email, GrantedGroups groups) {}
