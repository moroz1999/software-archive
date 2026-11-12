package com.archive.backend.search.web.dto;

public record QuickSearchDto(
        QuickSectionDto<SoftwareCatalogItemDto> software,
        QuickSectionDto<FoundAuthorDto> authors,
        QuickSectionDto<FoundGroupDto> groups) {}
