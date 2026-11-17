package com.archive.backend.user.app;

import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.UserId;

public record GetUserQuery(Actor actor, UserId id) {}
