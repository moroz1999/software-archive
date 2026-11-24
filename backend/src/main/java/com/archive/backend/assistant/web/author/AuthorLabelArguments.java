package com.archive.backend.assistant.web.author;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A label of the person: one they have, by its id, or a new one, by its name; one of them is the display name. */
public record AuthorLabelArguments(
        @Nullable UUID id, @Nullable String name, @Nullable Boolean display) {}
