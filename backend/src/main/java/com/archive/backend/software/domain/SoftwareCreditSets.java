package com.archive.backend.software.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds credits only from people, groups and labels that are in the archive, crediting an owner once: a person or a
 * group credited by themselves and under a label of theirs keeps only the credit under the label, with the roles of
 * both (docs/domain/software.md#credits).
 */
public class SoftwareCreditSets {

    private final SoftwareReferenceCatalog catalog;

    public SoftwareCreditSets(SoftwareReferenceCatalog catalog) {
        this.catalog = catalog;
    }

    public SoftwareCredits of(List<SoftwareCreditDraft> credits) {
        if (credits.stream().anyMatch(credit -> !catalog.contains(credit.participant()))) {
            throw new SoftwareCreditUnknownException();
        }
        return SoftwareCredits.of(
                joinOwnersIntoTheirLabels(SoftwareCredits.of(credits).all()));
    }

    private List<SoftwareCreditDraft> joinOwnersIntoTheirLabels(List<SoftwareCreditDraft> credits) {
        Map<SoftwareReference, SoftwareCreditDraft> joined = new LinkedHashMap<>();
        credits.forEach(credit -> joined.put(credit.participant(), credit));
        credits.stream()
                .filter(credit -> credit.participant().isOwner())
                .forEach(owner -> catalog.labelsOf(owner.participant()).stream()
                        .map(SoftwareReference::label)
                        .filter(joined::containsKey)
                        .findFirst()
                        .ifPresent(label -> {
                            joined.computeIfPresent(label, (key, credit) -> credit.joinedWith(owner));
                            joined.remove(owner.participant());
                        }));
        return List.copyOf(joined.values());
    }
}
