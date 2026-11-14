package com.archive.backend.software.domain;

/** The people, groups and labels that can be credited on software. */
public interface SoftwareReferenceCatalog {

    boolean contains(SoftwareReference reference);
}
