package com.archive.backend.importer.web.dto;

import java.util.UUID;

/** A program a run added; {@code exists} tells whether its software is still in the archive. */
public record ImportRunAddedDto(String externalId, UUID softwareId, String title, boolean exists) {}
