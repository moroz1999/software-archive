package com.archive.backend.assistant.web.label;

import java.util.UUID;

/** The arguments of merge_label_into_label. */
public record MergeLabelIntoLabelArguments(UUID id, UUID labelId) {}
