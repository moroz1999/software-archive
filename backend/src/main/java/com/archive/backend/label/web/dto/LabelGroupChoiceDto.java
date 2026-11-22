package com.archive.backend.label.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The group a label is given to: merged into or made a label of. */
public record LabelGroupChoiceDto(@Nullable UUID group) {}
