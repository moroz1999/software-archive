package com.archive.backend.label.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The owner a label of nobody is attached to: {@code author} or {@code group}, and whether it is its display name. */
public record LabelOwnerRequestDto(
        @Nullable String type, @Nullable UUID id, @Nullable Boolean display) {}
