package com.archive.backend.label.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The person a label is merged into. */
public record LabelAuthorChoiceDto(@Nullable UUID author) {}
