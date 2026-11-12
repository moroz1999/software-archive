package com.archive.backend.software.web.dto;

import java.util.UUID;

/** One chosen person, group or exact label as the software form shows it. */
public record SoftwareReferenceDto(UUID id, String type, String title) {}
