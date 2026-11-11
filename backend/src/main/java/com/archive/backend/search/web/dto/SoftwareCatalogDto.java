package com.archive.backend.search.web.dto;

import java.util.List;

/** A page of the catalog in the page contract of the API, with the values of its filters. */
public record SoftwareCatalogDto(
        List<SoftwareCatalogItemDto> content, PageMetadataDto page, SoftwareCatalogFacetsDto facets) {}
