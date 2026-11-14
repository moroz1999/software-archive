package com.archive.backend.importer.read;

import java.util.UUID;

/**
 * A program a run added.
 *
 * @param exists whether the software is still in the archive
 */
public record ImportRunAddedEntry(String externalId, UUID softwareId, String title, boolean exists) {}
