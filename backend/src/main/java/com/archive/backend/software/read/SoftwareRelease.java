package com.archive.backend.software.read;

import java.time.LocalDate;
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
        @Nullable LocalDate releaseDate,
        @Nullable String description,
        List<SoftwareHardware> hardware) {

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
        return new SoftwareRelease(id, title, version, type, releaseDate, description, shown);
    }
}
