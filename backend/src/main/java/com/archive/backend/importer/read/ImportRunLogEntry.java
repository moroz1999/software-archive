package com.archive.backend.importer.read;

/** What one line of the log of a run tells; its kind names which. */
public sealed interface ImportRunLogEntry
        permits ImportRunListReadEntry,
                ImportRunDownloadedEntry,
                ImportRunAddedEntry,
                ImportRunKnownEntry,
                ImportRunSkippedEntry,
                ImportRunFailedEntry,
                ImportRunAuthorEntry,
                ImportRunPlaceEntry,
                ImportRunWarningEntry {}
