package com.archive.backend.assistant.web.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record ChatPlanActionDto(
        int position,
        String tool,
        @JsonRawValue String arguments,
        List<Integer> dependsOn,
        String kind,
        String entityType,
        @JsonRawValue @Nullable String state,
        String status,
        @Nullable String result,
        @Nullable String error) {}
