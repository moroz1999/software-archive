package com.archive.backend.importer.read;

/**
 * What of a program a run could not take as the source gives it.
 *
 * @param url the page of the program in the source
 * @param kind what could not be taken, such as {@code genre} or {@code file-missing}
 * @param value what the source gives, as it gives it
 */
public record ImportRunWarningEntry(String externalId, String url, String title, String kind, String value)
        implements ImportRunLogEntry {}
