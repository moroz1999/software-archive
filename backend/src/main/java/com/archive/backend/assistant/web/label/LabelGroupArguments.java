package com.archive.backend.assistant.web.label;

import java.util.UUID;

/** A label and a group. */
public record LabelGroupArguments(UUID id, UUID groupId) {}
