package com.archive.backend.importer.app;

import com.archive.backend.user.domain.Actor;

/** The report of the run of an import started last. */
public record GetLastImportRunQuery(Actor actor) {}
