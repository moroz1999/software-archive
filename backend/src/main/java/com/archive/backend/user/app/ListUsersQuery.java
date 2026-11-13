package com.archive.backend.user.app;

import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.UserSearch;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

public record ListUsersQuery(Actor actor, @Nullable UserSearch search, Pageable pageable) {}
