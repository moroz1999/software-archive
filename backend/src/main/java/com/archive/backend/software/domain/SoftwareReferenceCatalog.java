package com.archive.backend.software.domain;

import java.util.List;

/** The people, groups and labels that can be credited on software. */
public interface SoftwareReferenceCatalog {

    boolean contains(SoftwareReference reference);

    /**
     * The labels of a person or a group, in the order their own credit goes into one of them: the display name of a
     * person first, then by name (docs/domain/software.md#credits).
     */
    List<SoftwareReferenceId> labelsOf(SoftwareReference owner);
}
