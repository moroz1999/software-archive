package com.archive.backend.search.web.mapper;

import com.archive.backend.search.web.dto.CardScreenshotDto;
import java.util.UUID;

/** The screenshots of the cards, as every mapper of the search answers them. */
public final class CardScreenshotWebMapper {

    private CardScreenshotWebMapper() {}

    public static CardScreenshotDto toScreenshot(UUID id) {
        return new CardScreenshotDto(id, "/api/screenshots/" + id);
    }
}
