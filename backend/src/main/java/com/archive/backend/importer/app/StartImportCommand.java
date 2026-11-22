package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportSection;
import com.archive.backend.importer.domain.run.ImportSource;
import com.archive.backend.user.domain.Actor;
import org.jspecify.annotations.Nullable;

/**
 * Starts the import of a source archive in the background: of every program of it, or of one section of it.
 */
public record StartImportCommand(
        Actor actor, ImportSource source, @Nullable ImportSection section) {}
