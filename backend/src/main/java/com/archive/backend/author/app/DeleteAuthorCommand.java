package com.archive.backend.author.app;

import com.archive.backend.author.domain.AuthorId;

public record DeleteAuthorCommand(AuthorId id) {}
