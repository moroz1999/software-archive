package com.archive.backend.assistant.web.entry;

import java.util.UUID;

/** The arguments of get_release. */
public record GetReleaseArguments(UUID softwareId, UUID releaseId) {}
