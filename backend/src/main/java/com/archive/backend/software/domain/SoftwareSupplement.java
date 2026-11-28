package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.EnumCodes;
import com.archive.backend.shared.domain.ddd.ValueObject;

/** What an import added to software it found: each a part the software lacked (docs/domain/imports.md). */
@ValueObject
public enum SoftwareSupplement {
    DESCRIPTION,
    RELEASE_DATE,
    PRESERVATION,
    CATEGORIES,
    CREDITS,
    SCREENSHOTS,
    LINEAGE;

    /** The code it is known by outside the backend: {@code RELEASE_DATE} as {@code release-date}. */
    public String code() {
        return EnumCodes.kebab(this);
    }
}
