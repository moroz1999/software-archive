package com.archive.backend.assistant.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatPlanDto(UUID id, String title, String status, Instant proposedAt, List<ChatPlanActionDto> actions) {}
