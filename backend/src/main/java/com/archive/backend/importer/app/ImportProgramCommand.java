package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.source.SourceProgram;

/** Takes in one program of a source archive by the rules every source follows. */
public record ImportProgramCommand(ImportRunId run, SourceProgram program) {}
