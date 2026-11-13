package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.vektor.VektorSection;
import org.jspecify.annotations.Nullable;

/** Imports every program of vektor-06c.ru, or of one of its sections. */
public record ImportVektorCommand(@Nullable VektorSection section) {}
