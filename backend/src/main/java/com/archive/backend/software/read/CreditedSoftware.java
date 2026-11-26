package com.archive.backend.software.read;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Software a person or a group is credited on, as their page lists it: what its card shows, its rating among it,
 * and their credits on it.
 */
public record CreditedSoftware(
        UUID id,
        String title,
        @Nullable String releaseDate,
        List<SoftwareScreenshot> screenshots,
        List<String> madeBy,
        List<OwnCredit> credits,
        double rating,
        int votesAmount) {

    public CreditedSoftware(
            UUID id,
            String title,
            @Nullable String releaseDate,
            List<SoftwareScreenshot> screenshots,
            List<String> madeBy,
            List<OwnCredit> credits) {
        this(id, title, releaseDate, screenshots, madeBy, credits, 0, 0);
    }

    /**
     * One credit of the person or the group on the software: by themselves or under a label, which is named only when
     * it is not their main name.
     */
    public record OwnCredit(@Nullable String as, List<String> roles) {}
}
