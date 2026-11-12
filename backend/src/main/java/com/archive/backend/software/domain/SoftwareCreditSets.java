package com.archive.backend.software.domain;

import com.archive.backend.author.domain.PersonReferenceCatalog;
import java.util.List;

/** Builds credits only from people and aliases that still exist in the archive. */
public class SoftwareCreditSets {

    private final PersonReferenceCatalog catalog;

    public SoftwareCreditSets(PersonReferenceCatalog catalog) {
        this.catalog = catalog;
    }

    public SoftwareCredits of(List<SoftwareCreditDraft> credits) {
        if (credits.stream().anyMatch(credit -> !catalog.contains(credit.person()))) {
            throw new SoftwareCreditUnknownException();
        }
        return SoftwareCredits.of(credits);
    }
}
