package com.archive.backend.importer.web.dto;

/** A program a run could not add. */
public record ImportRunFailedDto(String externalId, String url, String reason) {}
