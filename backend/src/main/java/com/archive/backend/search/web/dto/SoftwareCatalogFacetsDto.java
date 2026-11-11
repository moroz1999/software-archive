package com.archive.backend.search.web.dto;

import java.util.List;

public record SoftwareCatalogFacetsDto(
        List<FacetValueDto<Integer>> years,
        List<FacetValueDto<String>> categories,
        List<FacetValueDto<String>> countries) {}
