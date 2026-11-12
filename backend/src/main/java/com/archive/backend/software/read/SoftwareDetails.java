package com.archive.backend.software.read;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * One software with everything shown about it; its base hardware by group, then by name; its
 * categories in the order of the tree.
 */
public record SoftwareDetails(
        UUID id,
        String title,
        @Nullable String description,
        @Nullable LocalDate releaseDate,
        SoftwarePlatform platform,
        List<SoftwareHardware> hardware,
        List<SoftwareCategory> categories,
        List<SoftwareReferenceOption> publishers,
        List<SoftwareReferenceOption> developers,
        List<SoftwareCredit> credits) {

    public SoftwareDetails(
            UUID id,
            String title,
            @Nullable String description,
            @Nullable LocalDate releaseDate,
            SoftwarePlatform platform,
            List<SoftwareHardware> hardware,
            List<SoftwareCategory> categories,
            List<SoftwareReferenceOption> publishers,
            List<SoftwareReferenceOption> developers) {
        this(id, title, description, releaseDate, platform, hardware, categories, publishers, developers, List.of());
    }

    public SoftwareDetails(
            UUID id,
            String title,
            @Nullable String description,
            @Nullable LocalDate releaseDate,
            SoftwarePlatform platform,
            List<SoftwareHardware> hardware,
            List<SoftwareCategory> categories) {
        this(id, title, description, releaseDate, platform, hardware, categories, List.of(), List.of(), List.of());
    }
}
