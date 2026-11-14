package com.archive.backend.software.app;

import com.archive.backend.author.domain.AuthorId;

/** The software a person is credited on, for their page. */
public record ListAuthorSoftwareQuery(AuthorId author) {}
