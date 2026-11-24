package com.archive.backend.assistant.web.label;

import java.util.UUID;

/** The arguments of merge_label_into_author. */
public record MergeLabelIntoAuthorArguments(UUID id, UUID authorId) {}
