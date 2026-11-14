package com.archive.backend.importer.domain.run;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** What one program of the source came to in a run. */
public sealed interface ImportOutcome {

    /** Taken into the archive. */
    @ValueObject
    record Added(AddedProgram program) implements ImportOutcome {}

    /** Taken by an earlier run. */
    @ValueObject
    record Known() implements ImportOutcome {}

    /** Left out by the rules of the import. */
    @ValueObject
    record Skipped() implements ImportOutcome {}

    /** Not taken: it failed. */
    @ValueObject
    record Failed(FailedProgram program) implements ImportOutcome {}
}
