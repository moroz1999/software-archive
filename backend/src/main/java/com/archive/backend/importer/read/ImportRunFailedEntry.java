package com.archive.backend.importer.read;

/** A program a run could not add: its page in the source archive and why. */
public record ImportRunFailedEntry(String externalId, String url, String reason) {}
