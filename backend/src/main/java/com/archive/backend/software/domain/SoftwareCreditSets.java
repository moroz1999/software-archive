package com.archive.backend.software.domain;

import java.util.List;

/** Builds credits only from people, groups and labels that are in the archive. */
public class SoftwareCreditSets {

    private final SoftwareReferenceCatalog catalog;

    public SoftwareCreditSets(SoftwareReferenceCatalog catalog) {
        this.catalog = catalog;
    }

    public SoftwareCredits of(List<SoftwareCreditDraft> credits) {
        if (credits.stream().anyMatch(credit -> !catalog.contains(credit.participant()))) {
            throw new SoftwareCreditUnknownException();
        }
        return SoftwareCredits.of(credits);
    }
}
