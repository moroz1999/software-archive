package com.archive.backend.importer.domain.run;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** Why a run left a program out: the section of the source, or a category of it, that the import does not take. */
@ValueObject
public enum SkipReason {
    SECTION,
    CATEGORY
}
