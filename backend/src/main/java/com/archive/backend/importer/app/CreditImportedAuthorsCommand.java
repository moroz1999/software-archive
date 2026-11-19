package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.source.SourceProgram;

/** The authors of a program of a source, to credit them as whom they stand for in the archive. */
public record CreditImportedAuthorsCommand(SourceProgram program) {}
