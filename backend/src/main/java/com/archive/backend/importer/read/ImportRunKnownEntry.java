package com.archive.backend.importer.read;

import java.util.List;
import java.util.UUID;

/**
 * A program a run found in the archive.
 *
 * @param exists whether the software is still in the archive
 * @param supplements what was added to it: {@code description}, {@code release-date}, {@code preservation},
 *     {@code categories}, {@code credits}, {@code screenshots}; none when it lacked nothing
 */
public record ImportRunKnownEntry(
        String externalId, UUID softwareId, String title, boolean exists, List<String> supplements) {}
