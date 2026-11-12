package com.archive.backend.search.web.dto;

import java.util.List;

/** A page of a section of the full search in the page contract of the API. */
public record FoundPageDto<T>(List<T> content, PageMetadataDto page) {}
