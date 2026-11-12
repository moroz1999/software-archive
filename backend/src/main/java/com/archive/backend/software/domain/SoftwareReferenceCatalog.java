package com.archive.backend.software.domain;

/** The people, groups and owned labels that can be chosen for software. */
public interface SoftwareReferenceCatalog {

    boolean contains(SoftwareReference reference);
}
