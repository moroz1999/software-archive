package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ExternalPage;

/** The page of another site whose software is looked for. */
public record FindSoftwareOfPageQuery(ExternalPage page) {}
