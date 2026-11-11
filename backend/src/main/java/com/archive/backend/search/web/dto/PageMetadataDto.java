package com.archive.backend.search.web.dto;

/** The metadata of the API's page contract (docs/backend.md#shared-building-blocks). */
public record PageMetadataDto(long size, long number, long totalElements, long totalPages) {}
