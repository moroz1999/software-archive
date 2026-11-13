package com.archive.backend.author.app;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.user.domain.Actor;

public record DeleteAuthorCommand(Actor actor, AuthorId id) {}
