package com.archive.backend.importer.read;

/**
 * A file of a program a run downloaded from the source.
 *
 * @param title the title of the program in the source
 * @param file {@code archive} or {@code screenshot}
 * @param name the name of the file in the source
 * @param size its size in bytes
 */
public record ImportRunDownloadedEntry(String externalId, String title, String file, String name, long size)
        implements ImportRunLogEntry {}
