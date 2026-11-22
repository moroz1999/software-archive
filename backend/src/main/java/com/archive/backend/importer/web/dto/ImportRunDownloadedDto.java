package com.archive.backend.importer.web.dto;

/** As {@code ImportRunDownloadedEntry} of the report. */
public record ImportRunDownloadedDto(String externalId, String title, String file, String name, long size) {}
