package com.archive.backend.software.web.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Software on the page of a person or a group: its card, and their credits on it. */
public record CreditedSoftwareDto(
        UUID id,
        String title,
        @Nullable String releaseDate,
        List<ScreenshotDto> screenshots,
        List<String> madeBy,
        List<OwnCreditDto> credits) {

    /** {@code as} names the label of the credit when it is not the main name of the person or the group. */
    public record OwnCreditDto(@Nullable String as, List<String> roles) {}
}
