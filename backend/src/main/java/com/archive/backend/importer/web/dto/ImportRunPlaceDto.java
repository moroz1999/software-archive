package com.archive.backend.importer.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** As {@code ImportRunPlaceEntry} of the report. */
public record ImportRunPlaceDto(@Schema(ref = "PlaceKind") String kind, String name) {}
