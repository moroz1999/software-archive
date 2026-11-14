package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.user.domain.Actor;
import org.jspecify.annotations.Nullable;

/** Starts the import of every program of vektor-06c.ru, or of one of its sections, in the background. */
public record StartVektorImportCommand(
        Actor actor, @Nullable VektorSection section) {}
