package com.archive.backend.software.domain;

import java.util.Set;

/** Builds software references only from people, groups and owned labels that are in the archive. */
public class SoftwareReferenceSets {

    private final SoftwareReferenceCatalog catalog;

    public SoftwareReferenceSets(SoftwareReferenceCatalog catalog) {
        this.catalog = catalog;
    }

    public SoftwareReferences of(Set<SoftwareReference> publishers, Set<SoftwareReference> developers) {
        SoftwareReferences references = new SoftwareReferences(publishers, developers);
        if (publishers.stream().anyMatch(reference -> !catalog.contains(reference))) {
            throw new SoftwarePublisherUnknownException();
        }
        if (developers.stream().anyMatch(reference -> !catalog.contains(reference))) {
            throw new SoftwareDeveloperUnknownException();
        }
        return references;
    }
}
