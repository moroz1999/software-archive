package com.archive.backend.assistant.web.remote;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A picture of another site by its address, or a screenshot of the archive by its id: one of them. */
public record ViewImageArguments(
        @Nullable String url, @Nullable UUID screenshotId) {}
