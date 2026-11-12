package com.archive.backend.software.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** One person, group or label sent by a software form. */
public record SoftwareReferenceRequestDto(
        @Nullable UUID id, @Nullable String type) {}
