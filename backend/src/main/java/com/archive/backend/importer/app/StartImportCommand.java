package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportSource;
import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.user.domain.Actor;
import org.jspecify.annotations.Nullable;

/**
 * Starts the import of a source archive in the background: of every program of it, or of one section of vektor-06c.ru;
 * "Базис" has no sections.
 */
public record StartImportCommand(
        Actor actor, ImportSource source, @Nullable VektorSection section) {}
