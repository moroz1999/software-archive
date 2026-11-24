package com.archive.backend.assistant.web.software;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of set_software_credit. */
public record SoftwareCreditArguments(
        UUID softwareId, UUID id, String type, @Nullable List<String> roles) {}
