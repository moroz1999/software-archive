package com.archive.backend.importer.domain.run;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** What one program of the source came to in a run. */
public sealed interface ImportOutcome {

    /** Taken into the archive. */
    @ValueObject
    record Added(AddedProgram program, TakenAuthors taken) implements ImportOutcome {}

    /** Found in the archive, taken by an earlier run or matched by its files; what it lacked is added to it. */
    @ValueObject
    record Known(KnownProgram program, TakenAuthors taken) implements ImportOutcome {}

    /** Left out by the rules of the import. */
    @ValueObject
    record Skipped(SkippedProgram program) implements ImportOutcome {}

    /** Not taken: it failed. */
    @ValueObject
    record Failed(FailedProgram program) implements ImportOutcome {}
}
