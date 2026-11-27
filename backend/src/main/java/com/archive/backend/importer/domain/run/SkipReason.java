package com.archive.backend.importer.domain.run;

import com.archive.backend.shared.domain.EnumCodes;
import com.archive.backend.shared.domain.ddd.ValueObject;

/** Why a run left a program out: the section of the source, or a category of it, that the import does not take. */
@ValueObject
public enum SkipReason {
    SECTION,
    CATEGORY;

    /** The code it is known by outside the backend: {@code SECTION} as {@code section}. */
    public String code() {
        return EnumCodes.kebab(this);
    }
}
