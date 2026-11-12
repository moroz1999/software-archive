package com.archive.backend.search.web.dto;

public record QuickSearchDto(
        QuickSectionDto<SoftwareCatalogItemDto> software,
        QuickSectionDto<QuickAuthorDto> authors,
        QuickSectionDto<QuickGroupDto> groups) {}
