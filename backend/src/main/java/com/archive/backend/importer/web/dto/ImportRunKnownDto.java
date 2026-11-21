package com.archive.backend.importer.web.dto;

import java.util.List;
import java.util.UUID;

/** As {@code ImportRunKnownEntry} of the report. */
public record ImportRunKnownDto(
        String externalId, UUID softwareId, String title, boolean exists, List<String> supplements) {}
