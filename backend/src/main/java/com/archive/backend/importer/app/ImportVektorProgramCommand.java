package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.vektor.VektorProgram;

/** Adds one program of vektor-06c.ru the archive has not taken yet. */
public record ImportVektorProgramCommand(VektorProgram program) {}
