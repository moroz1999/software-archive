package com.archive.backend.assistant.web.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public record ChatPlanActionDto(
        int position,
        String tool,

        @JsonRawValue @Schema(implementation = Map.class, description = "The arguments of the tool call")
        String arguments,

        List<Integer> dependsOn,
        String kind,
        String entityType,

        @JsonRawValue
        @Schema(
                types = {"object", "array"},
                description = "The entry as it was when the plan was proposed")
        @Nullable
        String state,

        @Schema(ref = "PlanActionStatus") String status,
        @Nullable String result,
        @Nullable String error) {}
