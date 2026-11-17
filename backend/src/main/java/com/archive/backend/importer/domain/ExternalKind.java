package com.archive.backend.importer.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

/**
 * What kind of entry of the archive an import added.
 */
@ValueObject
public enum ExternalKind {
    SOFTWARE,
    AUTHOR,
    LABEL
}
