package com.archive.backend.assistant.web.software;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of set_release_credit. */
public record ReleaseCreditArguments(
        UUID softwareId,
        UUID releaseId,
        UUID id,
        String type,
        @Nullable List<String> roles) {}
