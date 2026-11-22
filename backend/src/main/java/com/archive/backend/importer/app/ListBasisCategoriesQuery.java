package com.archive.backend.importer.app;

import com.archive.backend.user.domain.Actor;

/** Lists the categories of "Базис" whose cards the archive can import. */
public record ListBasisCategoriesQuery(Actor actor) {}
