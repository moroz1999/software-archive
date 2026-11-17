package com.archive.backend.label.web.dto;

import java.util.List;
import org.jspecify.annotations.Nullable;

/** Body of adding a label of nobody: its name and the whole list of its locations. */
public record NewLabelRequestDto(
        @Nullable String name, @Nullable List<LabelRequestDto.LocationDto> locations) {}
