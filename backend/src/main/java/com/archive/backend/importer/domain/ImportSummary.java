package com.archive.backend.importer.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

/**
 * What a run of an import did: how many entries of the source it added, found already added, left out by the rules
 * and failed to add.
 */
@ValueObject
public record ImportSummary(int added, int known, int skipped, int failed) {}
