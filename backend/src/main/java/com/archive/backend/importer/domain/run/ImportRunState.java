package com.archive.backend.importer.domain.run;

import com.archive.backend.shared.domain.EnumCodes;
import com.archive.backend.shared.domain.ddd.ValueObject;

/** Where a run of an import is: going on, through to the end, or stopped before it. */
@ValueObject
public enum ImportRunState {
    RUNNING,
    DONE,
    STOPPED;

    /** The code it is known by outside the backend: {@code RUNNING} as {@code running}. */
    public String code() {
        return EnumCodes.kebab(this);
    }
}
