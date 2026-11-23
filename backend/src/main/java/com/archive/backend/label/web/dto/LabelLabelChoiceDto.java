package com.archive.backend.label.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The other label a label is merged into. */
public record LabelLabelChoiceDto(@Nullable UUID label) {}
