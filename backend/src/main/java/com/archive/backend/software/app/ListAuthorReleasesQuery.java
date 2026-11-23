package com.archive.backend.software.app;

import com.archive.backend.author.domain.AuthorId;

/** The releases a person is credited on, for their page. */
public record ListAuthorReleasesQuery(AuthorId author) {}
