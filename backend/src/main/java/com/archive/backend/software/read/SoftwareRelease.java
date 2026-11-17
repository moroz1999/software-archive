package com.archive.backend.software.read;

import com.archive.backend.user.read.Addition;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * A release of software, under its own title or the title of its software.
 */
public record SoftwareRelease(
        UUID id,
        String title,
        @Nullable String version,
        String type,
        @Nullable String releaseDate,
        @Nullable String description,
        List<SoftwareHardware> hardware,
        List<SoftwareScreenshot> screenshots,
        @Nullable ReleaseArchiveDetails archive,
        Addition added,
        @Nullable UUID addedBy,
        boolean canChange) {

    /** As read, before the handler tells whether the actor may change it. */
    public SoftwareRelease(
            UUID id,
            String title,
            @Nullable String version,
            String type,
            @Nullable String releaseDate,
            @Nullable String description,
            List<SoftwareHardware> hardware,
            Addition added) {
        this(id, title, version, type, releaseDate, description, hardware, List.of(), null, added, null, false);
    }

    public SoftwareRelease(
            UUID id,
            String title,
            @Nullable String version,
            String type,
            @Nullable String releaseDate,
            @Nullable String description,
            List<SoftwareHardware> hardware,
            List<SoftwareScreenshot> screenshots,
            Addition added,
            @Nullable UUID addedBy,
            boolean canChange) {
        this(
                id,
                title,
                version,
                type,
                releaseDate,
                description,
                hardware,
                screenshots,
                null,
                added,
                addedBy,
                canChange);
    }

    /** The same release, telling whether the actor may change it. */
    public SoftwareRelease withCanChange(boolean canChange) {
        return new SoftwareRelease(
                id,
                title,
                version,
                type,
                releaseDate,
                description,
                hardware,
                screenshots,
                archive,
                added,
                addedBy,
                canChange);
    }

    /**
     * The release with the hardware it shows over the base hardware of its software: every group the release has
     * replaces that group of the base entirely, the other groups come from the base (docs/domain/hardware.md).
     */
    public SoftwareRelease over(List<SoftwareHardware> base) {
        Set<String> own = hardware.stream().map(SoftwareHardware::group).collect(Collectors.toSet());
        List<SoftwareHardware> shown = Stream.concat(
                        base.stream().filter(item -> !own.contains(item.group())), hardware.stream())
                .sorted(Comparator.comparingInt(SoftwareHardware::groupOrder))
                .toList();
        return new SoftwareRelease(
                id,
                title,
                version,
                type,
                releaseDate,
                description,
                shown,
                screenshots,
                archive,
                added,
                addedBy,
                canChange);
    }
}
