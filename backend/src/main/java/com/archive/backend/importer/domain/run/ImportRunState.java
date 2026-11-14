package com.archive.backend.importer.domain.run;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** Where a run of an import is: going on, through to the end, or stopped before it. */
@ValueObject
public enum ImportRunState {
    RUNNING,
    DONE,
    STOPPED
}
