package com.archive.backend.importer.domain.source;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** What a source tells an author is: only a name, a person or a group. */
@ValueObject
public enum SourceAuthorKind {
    LABEL,
    PERSON,
    GROUP
}
