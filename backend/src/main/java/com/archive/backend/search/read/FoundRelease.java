package com.archive.backend.search.read;

import com.archive.backend.software.read.SoftwareCredit;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A release found by the release search, with its software and what tells it apart from the other releases. */
public record FoundRelease(
        UUID id,
        UUID softwareId,
        String softwareTitle,
        String platform,
        @Nullable String title,
        @Nullable String version,
        String type,
        @Nullable String releaseDate,
        List<String> hardware,
        @Nullable String archiveName,
        @Nullable Long archiveSize,
        List<SoftwareCredit> credits) {}
