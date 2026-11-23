package com.archive.backend.software.read;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * A release a person, a group or a label is credited on, as their page lists it: the release under its own title or
 * the title of its software, its software, and their credits on it.
 */
public record CreditedRelease(
        UUID id,
        UUID softwareId,
        String softwareTitle,
        String title,
        @Nullable String version,
        String type,
        @Nullable String releaseDate,
        List<SoftwareScreenshot> screenshots,
        List<CreditedSoftware.OwnCredit> credits) {}
