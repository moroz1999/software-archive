package com.archive.backend.importer.web.dto;

import java.util.UUID;

/** As {@code ImportRunAuthorEntry} of the report. */
public record ImportRunAuthorDto(String kind, UUID id, String name, boolean created, boolean exists) {}
