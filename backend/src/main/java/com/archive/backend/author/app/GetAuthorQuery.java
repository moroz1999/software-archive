package com.archive.backend.author.app;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.Actor;

public record GetAuthorQuery(Actor actor, AuthorId id, Language language) {}
