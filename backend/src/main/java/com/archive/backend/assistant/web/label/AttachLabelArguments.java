package com.archive.backend.assistant.web.label;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of attach_label. */
public record AttachLabelArguments(
        UUID id, String ownerType, UUID ownerId, @Nullable Boolean display) {}
