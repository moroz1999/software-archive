package com.archive.backend.search.read;

import org.springframework.data.domain.Page;

/** A page of the software catalog with the values of its filters. */
public record SoftwareCatalogPage(Page<SoftwareCatalogItem> items, SoftwareCatalogFacets facets) {}
