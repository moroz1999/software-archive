package com.archive.backend.assistant.web.software;

import java.util.UUID;

/** The arguments of remove_software_credit. */
public record RemoveSoftwareCreditArguments(UUID softwareId, UUID id) {}
