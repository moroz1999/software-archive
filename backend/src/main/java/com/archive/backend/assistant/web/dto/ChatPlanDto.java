package com.archive.backend.assistant.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatPlanDto(
        UUID id,
        String title,
        @Schema(ref = "PlanStatus") String status,
        Instant proposedAt,
        List<ChatPlanActionDto> actions) {}
