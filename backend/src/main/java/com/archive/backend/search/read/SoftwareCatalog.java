package com.archive.backend.search.read;

import org.springframework.data.domain.Pageable;

/** The software catalog as the search engine serves it, sorted by title (docs/features/search.md#reads). */
public interface SoftwareCatalog {

    /** A page of the software the filter leaves, with the values of every filter; fails when the engine is down. */
    SoftwareCatalogPage find(SoftwareCatalogFilter filter, Pageable pageable);
}
