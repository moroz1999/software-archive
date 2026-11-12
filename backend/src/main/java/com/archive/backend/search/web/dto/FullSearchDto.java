package com.archive.backend.search.web.dto;

import java.util.List;

/** A page of the full search: the matches of every kind on it, and the page metadata of the API's page contract. */
public record FullSearchDto(List<FoundSetDto> sets, PageMetadataDto page) {}
