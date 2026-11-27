package com.archive.backend.importer.domain;

import com.archive.backend.shared.domain.EnumCodes;
import com.archive.backend.shared.domain.ddd.ValueObject;

/**
 * What kind of entry of the archive an import added.
 */
@ValueObject
public enum ExternalKind {
    SOFTWARE,
    AUTHOR,
    GROUP,
    LABEL;

    /** The code it is known by outside the backend: {@code AUTHOR} as {@code author}. */
    public String code() {
        return EnumCodes.kebab(this);
    }
}
