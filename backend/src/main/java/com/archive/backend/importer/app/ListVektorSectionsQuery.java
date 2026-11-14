package com.archive.backend.importer.app;

import com.archive.backend.user.domain.Actor;

/** The sections of vektor-06c.ru an import can be started for. */
public record ListVektorSectionsQuery(Actor actor) {}
