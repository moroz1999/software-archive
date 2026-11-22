package com.archive.backend.importer.read;

/**
 * A program a run left out.
 *
 * @param reason {@code section} or {@code category}: what of the source the import does not take
 * @param detail the name of that section or category
 */
public record ImportRunSkippedEntry(String externalId, String url, String title, String reason, String detail)
        implements ImportRunLogEntry {}
