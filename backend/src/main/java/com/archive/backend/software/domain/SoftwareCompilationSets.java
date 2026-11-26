package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.Set;

public class SoftwareCompilationSets {

    private static final String FIELD = "compilationItemIds";

    private final SoftwareCompilationCatalog catalog;

    public SoftwareCompilationSets(SoftwareCompilationCatalog catalog) {
        this.catalog = catalog;
    }

    public SoftwareCompilationItems of(SoftwareId compilation, Set<SoftwareId> ids) {
        if (!ids.isEmpty()) {
            catalog.lockChanges();
            if (ids.contains(compilation) || catalog.includes(compilation, ids)) {
                throw new RuleViolationException(
                        "software.compilation-cycle", "This selection would create a cycle of compilations.", FIELD);
            }
            if (!catalog.allExist(ids)) {
                throw new RuleViolationException(
                        "software.compilation-item-unknown", "A program is no longer in the archive.", FIELD);
            }
        }
        return new SoftwareCompilationItems(compilation, ids);
    }
}
