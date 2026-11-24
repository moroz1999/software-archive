package com.archive.backend.assistant.web.software;

import java.util.UUID;

/** The arguments of remove_release_credit. */
public record RemoveReleaseCreditArguments(UUID softwareId, UUID releaseId, UUID id) {}
