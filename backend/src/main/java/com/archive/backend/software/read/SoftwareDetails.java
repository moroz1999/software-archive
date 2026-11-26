package com.archive.backend.software.read;

import com.archive.backend.shared.read.ExternalOrigin;
import com.archive.backend.user.read.Addition;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * One software with everything shown about it; its base hardware by group, then by name; its categories in the order
 * of the tree; everyone credited on it; the pages of the archives it was imported from, and its ids there; the code
 * of its preservation status.
 */
public record SoftwareDetails(
        UUID id,
        String title,
        @Nullable String description,
        @Nullable String instructions,
        @Nullable String releaseDate,
        String preservation,
        SoftwarePlatform platform,
        List<SoftwareHardware> hardware,
        List<SoftwareCategory> categories,
        List<SoftwareCredit> credits,
        List<SoftwareSource> sources,
        List<ExternalOrigin> origins,
        Addition added,
        @Nullable UUID addedBy,
        boolean canChange,
        List<SoftwareLink> compilationItems,
        List<SoftwareLink> compilations) {

    public SoftwareDetails(
            UUID id,
            String title,
            @Nullable String description,
            @Nullable String instructions,
            @Nullable String releaseDate,
            String preservation,
            SoftwarePlatform platform,
            List<SoftwareHardware> hardware,
            List<SoftwareCategory> categories,
            List<SoftwareCredit> credits,
            List<SoftwareSource> sources,
            List<ExternalOrigin> origins,
            Addition added,
            @Nullable UUID addedBy,
            boolean canChange) {
        this(
                id,
                title,
                description,
                instructions,
                releaseDate,
                preservation,
                platform,
                hardware,
                categories,
                credits,
                sources,
                origins,
                added,
                addedBy,
                canChange,
                List.of(),
                List.of());
    }

    /** As read, before the handler tells whether the actor may change it. */
    public SoftwareDetails(
            UUID id,
            String title,
            @Nullable String description,
            @Nullable String releaseDate,
            SoftwarePlatform platform,
            List<SoftwareHardware> hardware,
            List<SoftwareCategory> categories,
            List<SoftwareCredit> credits,
            Addition added) {
        this(
                id,
                title,
                description,
                null,
                releaseDate,
                "unknown",
                platform,
                hardware,
                categories,
                credits,
                List.of(),
                List.of(),
                added,
                null,
                false);
    }

    public SoftwareDetails(
            UUID id,
            String title,
            @Nullable String description,
            @Nullable String releaseDate,
            SoftwarePlatform platform,
            List<SoftwareHardware> hardware,
            List<SoftwareCategory> categories,
            Addition added) {
        this(id, title, description, releaseDate, platform, hardware, categories, List.of(), added);
    }

    /** The same software, telling whether the actor may change it. */
    public SoftwareDetails withCanChange(boolean canChange) {
        return new SoftwareDetails(
                id,
                title,
                description,
                instructions,
                releaseDate,
                preservation,
                platform,
                hardware,
                categories,
                credits,
                sources,
                origins,
                added,
                addedBy,
                canChange,
                compilationItems,
                compilations);
    }
}
